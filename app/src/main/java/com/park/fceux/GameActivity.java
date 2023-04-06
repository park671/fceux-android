package com.park.fceux;

import android.app.Activity;
import android.media.AudioTrack;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;

public class GameActivity extends Activity {

    private Benchmark.BenchmarkCallback benchmarkCallback = new Benchmark.BenchmarkCallback() {
        private int numTests = 0;
        private int numOk = 0;

        @Override
        public void onBenchmarkReset(Benchmark benchmark) {
        }

        @Override
        public void onBenchmarkEnded(Benchmark benchmark, int steps, long totalTime) {
            float millisPerFrame = totalTime / (float) steps;
            numTests++;
            if (true) {
                if (millisPerFrame < 17) {
                    numOk++;
                }
            }
        }
    };

    public static EmulatorRunner runner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        GameGLView glSurfaceView = findViewById(R.id.gameGLView);
        glSurfaceView.setEGLContextClientVersion(2);
        GameGLRenderer renderer = new GameGLRenderer();
        renderer.benchmark = new Benchmark("openGL", 200, benchmarkCallback);

        glSurfaceView.setRenderer(renderer);

        runner = new EmulatorRunner(GameActivity.this);
        runner.startGame();
    }
}
