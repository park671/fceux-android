package com.park.fceux;

import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class JniBridge {
    private static final String TAG = "JniBridge";

    private static final JniBridge sInstance = new JniBridge();

    private AudioTrack track;
    private short[] sfxBuffer;
    private int minSize;

    private final Object readyLock = new Object();
    private final Object loadLock = new Object();
    private static final int SIZE = 32768 * 2;
    private final short[][] testX = new short[2][SIZE];
    private int cur = 0;
    private int[] lenX = new int[2];
    private AtomicBoolean ready = new AtomicBoolean();
    private AtomicInteger totalWritten = new AtomicInteger();

    public void initSound(SfxProfile sfx) {
        sfxBuffer = new short[sfx.bufferSize];
        int format = sfx.isStereo ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO;
        int encoding = sfx.encoding == SfxProfile.SoundEncoding.PCM8 ?
                AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT;
        minSize = AudioTrack.getMinBufferSize(sfx.rate, format, encoding);
        track = new AudioTrack(AudioManager.STREAM_MUSIC, sfx.rate, format,
                encoding, minSize, AudioTrack.MODE_STREAM);
        try {
            track.play();
            resetTrack();

        } catch (Exception e) {
            throw e;
        }

        Log.d(TAG, "sound init OK");
    }

    public void readSfxData() {
        int length = readSfxBuffer(sfxBuffer);
        int slen;
        int back;

        synchronized (testX) {
            back = cur;
            slen = lenX[back];
            if (length > 0) {
                if (slen + length < SIZE) {
                    System.arraycopy(sfxBuffer, 0, testX[back], 0, length);
                    lenX[back] = length;
                } else {
                    lenX[back] = 0;
                }
            }
        }
    }

    public void renderSfx() {
        synchronized (readyLock) {
            if (track == null) {
                return;
            }
            int slen;
            int cur = this.cur;
            synchronized (testX) {
                slen = lenX[cur];

                if (slen > 0) {
                    lenX[cur] = 0;
                    this.cur = cur == 0 ? 1 : 0;
                }
            }

            if (slen > 0) {
                track.flush();
                track.write(testX[cur], 0, slen);
                totalWritten.set(slen);
            }
        }
    }

    private void resetTrack() {
        if (track != null) {
            track.flush();
            track.write(new short[minSize - 2], 0, minSize - 2);
        }
    }

    public static JniBridge getInstance() {
        return sInstance;
    }
    {
        System.loadLibrary("nes");
    }

    public native boolean setBaseDir(String path);

    public native boolean start(int gfx, int sfx, int general);

    public native boolean reset();

    public native boolean loadGame(String fileName, String batteryDir, String strippedName);

    public native boolean loadState(String fileName, int slot);

    public native boolean saveState(String fileName, int slot);

    public native int readSfxBuffer(short[] data);

    public native boolean enableCheat(String gg, int type);

    public native boolean enableRawCheat(int addr, int val, int comp);

    public native boolean fireZapper(int x, int y);

    public native boolean render(Bitmap bitmap);

    public native boolean renderVP(Bitmap bitmap, int vw, int vh);

    public native boolean renderHistory(Bitmap bitmap, int item, int vw, int vh);

    public native boolean renderGL();

    public native boolean emulate(int keys, int turbos, int numFramesToSkip);

    public native boolean readPalette(int[] result);

    public native boolean setViewPortSize(int w, int h);

    public native boolean stop();

    public native int getHistoryItemCount();

    public native boolean loadHistoryState(int pos);

}
