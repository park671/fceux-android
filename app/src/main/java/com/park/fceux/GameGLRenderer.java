package com.park.fceux;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameGLRenderer implements GLSurfaceView.Renderer {

    static String fragmentShaderCode = "precision mediump float;"
            + "varying vec2 v_texCoord;"
            + "uniform sampler2D s_texture;"
            + "uniform sampler2D s_palette; "
            + "void main()"
            + "{           "
            + "		 float a = texture2D(s_texture, v_texCoord).a;"
            + "	     float c = floor((a * 256.0) / 127.5);"
            + "      float x = a - c * 0.001953;"
            + "      vec2 curPt = vec2(x, 0);"
            + "      gl_FragColor.rgb = texture2D(s_palette, curPt).rgb;"
            + "}";

    static String vertexShaderCode = "attribute vec4 a_position; "
            + "attribute vec2 a_texCoord;  								 "
            + "uniform mat4 uMVPMatrix;   								 "
            + "varying lowp vec2 v_texCoord;   						     "
            + "void main()                  							 "
            + "{                            							 "
            + "   gl_Position =  uMVPMatrix  * a_position; 				 "
            + "   v_texCoord = a_texCoord;  							 "
            + "}                            							 ";

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
            String log = GLES20.glGetShaderInfoLog(shader);
            throw new RuntimeException("glCompileShader failed. t: " + type + " " + log + "#");
        }

        return shader;
    }

    int program;
    int paletteTextureId;
    int mainTextureId;

    private static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("GLView", glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    private void initTextures() {
        int numTextures = 2;
        int[] textureIds = new int[numTextures];
        int textureWidth = 256;
        int textureHeight = 256;
        int paletteSize = 256;
        GLES20.glGenTextures(numTextures, textureIds, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_ALPHA, textureWidth,
                textureHeight, 0, GLES20.GL_ALPHA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glPixelStorei(GLES20.GL_PACK_ALIGNMENT, 1);
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[1]);
        int[] palette = new int[paletteSize];
        JniBridge.getInstance().readPalette(palette);

        for (int i = 0; i < paletteSize; i++) {
            int dd = palette[i];
            int b = (dd & 0x00FF0000) >> 16;
            int g = (dd & 0x0000FF00) >> 8;
            int r = (dd & 0x000000FF) >> 0;
            palette[i] = 0xff000000 | (r << 16) | (g << 8) | b;
        }

        GLES20.glPixelStorei(GLES20.GL_PACK_ALIGNMENT, 1);
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
        Bitmap paletteBmp = Bitmap.createBitmap(paletteSize, paletteSize, Bitmap.Config.ARGB_8888);
        paletteBmp.setPixels(palette, 0, paletteSize, 0, 0, paletteSize, 1);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, paletteBmp, 0);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        paletteTextureId = textureIds[1];
        mainTextureId = textureIds[0];
        checkGlError("textures");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);

        if (linkStatus[0] != GLES20.GL_TRUE) {
            String log = GLES20.glGetProgramInfoLog(program);
            throw new RuntimeException("glLinkProgram failed. " + log + "#");
        }
        initTextures();
    }

    private float[] projMatrix = new float[16];
    int positionHandle, textureHandle, paletteHandle, texCoordHandle, mvpMatrixHandle;

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Matrix.orthoM(projMatrix, 0, -width / 2f, +width / 2f, -height / 2f,
                +height / 2f, -2f, 2f);
        int nvpy = (height - 0 - height);
        GLES20.glViewport(0, 0, width, height);
        initQuadCoordinates(height, height);
        GLES20.glUseProgram(program);
        positionHandle = GLES20.glGetAttribLocation(program, "a_position");
        textureHandle = GLES20.glGetUniformLocation(program, "s_texture");
        paletteHandle = GLES20.glGetUniformLocation(program, "s_palette");
        texCoordHandle = GLES20.glGetAttribLocation(program, "a_texCoord");
        startTime = System.currentTimeMillis();
    }

    int getTextureSize() {
        return 256;
    }

    private float[] quadCoords;
    private float[] textureCoords;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private ShortBuffer drawListBuffer;
    private final short[] drawOrder = {0, 1, 2, 0, 2, 3};

    private void initQuadCoordinates(int width, int height) {
        int maxTexX;
        int maxTexY;

        maxTexX = 256;
        maxTexY = 240;


        int textureSize = getTextureSize();
        quadCoords = new float[]{
                -width / 2f, -height / 2f, 0,
                -width / 2f, height / 2f, 0,
                width / 2f, height / 2f, 0,
                width / 2f, -height / 2f, 0
        };
        textureCoords = new float[]{
                0,
                maxTexY / (float) textureSize,
                0,
                0,
                maxTexX / (float) textureSize,
                0,
                maxTexX / (float) textureSize,
                maxTexY / (float) textureSize,
        };
        ByteBuffer bb1 = ByteBuffer.allocateDirect(quadCoords.length * 4);
        bb1.order(ByteOrder.nativeOrder());
        vertexBuffer = bb1.asFloatBuffer();
        vertexBuffer.put(quadCoords);
        vertexBuffer.position(0);
        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureCoords.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureBuffer = bb2.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
    }

    int delayPerFrame = 40;
    long startTime;

    public Benchmark benchmark;

    @Override
    public void onDrawFrame(GL10 gl) {
        benchmark.notifyFrameEnd();
        long endTime = System.currentTimeMillis();
        long delay = delayPerFrame - (endTime - startTime);

        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {
            }
        }
        startTime = System.currentTimeMillis();
        benchmark.notifyFrameStart();
        render();
    }

    static final int COORDS_PER_VERTEX = 3;
    static final int COORDS_PER_TEXTURE = 2;

    public final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;
    public final int TEXTURE_STRIDE = COORDS_PER_TEXTURE * 4;

    private void render() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(texCoordHandle);
        checkGlError("handles");
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, VERTEX_STRIDE, vertexBuffer);
        GLES20.glVertexAttribPointer(texCoordHandle, COORDS_PER_TEXTURE, GLES20.GL_FLOAT,
                false, TEXTURE_STRIDE, textureBuffer);
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, projMatrix, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mainTextureId);
        GLES20.glUniform1i(textureHandle, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, paletteTextureId);
        GLES20.glUniform1i(paletteHandle, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checkGlError("uniforms");
        JniBridge.getInstance().renderGL();
        checkGlError("emu render");
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
        checkGlError("disable vertex arrays");
    }
}
