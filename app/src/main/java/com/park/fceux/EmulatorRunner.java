package com.park.fceux;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EmulatorRunner {

    private static final String TAG = "EmulatorRunner";
    private static final int AUTO_SAVE_SLOT = 0;
    protected final Object lock = new Object();
    protected Context context;
    private Object pauseLock = new Object();
    private boolean audioEnabled = true;
    private Benchmark benchmark;
    private Object sfxReadyLock = new Object();
    private boolean sfxReady = false;
    private AudioPlayer audioPlayer;
    private AtomicBoolean isPaused = new AtomicBoolean();
    private EmulatorThread updater;
    private OnNotRespondingListener notRespondingListener;

    int keys = 0;

    public void setKeyPressed(int port, int key, boolean isPressed) {
        int n = port * 8;
        if (isPressed) {
            keys |= (key << n);
        } else {
            keys &= ~(key << n);
        }
    }

    public EmulatorRunner(Context context) {
        this.context = context;
    }

    public void destroy() {
        if (audioPlayer != null) {
            audioPlayer.destroy();
        }

        if (updater != null) {
            updater.destroy();
        }
    }

    public void setOnNotRespondingListener(OnNotRespondingListener listener) {
        notRespondingListener = listener;
    }

    public void pauseEmulation() {
        synchronized (pauseLock) {
            if (!isPaused.get()) {
                Log.i(TAG, "--PAUSE EMULATION--");
                isPaused.set(true);
                updater.pause();
            }
        }
    }

    public void resumeEmulation() {
        synchronized (pauseLock) {
            if (isPaused.get()) {
                Log.i(TAG, "--UNPAUSE EMULATION--");
                updater.unpause();
                isPaused.set(false);
            }
        }
    }

    public void stopGame() {
        if (audioPlayer != null) {
            audioPlayer.destroy();
        }

        if (updater != null) {
            updater.destroy();
        }

        synchronized (lock) {
            JniBridge.getInstance().stop();
        }
    }

    public void resetEmulator() {
        synchronized (lock) {
            JniBridge.getInstance().reset();
        }
    }

    public void startGame() {
        isPaused.set(false);

        if (updater != null) {
            updater.destroy();
        }

        if (audioPlayer != null) {
            audioPlayer.destroy();
        }

        File file = new File(context.getCacheDir(), "METALMAX.nes");
        FileUtil.copyFileFromAssets(context, "METALMAX.nes", file.getParent(), file.getName());
        JniBridge bridge = JniBridge.getInstance();
        SfxProfile sfxProfile = NesInfo.sfxProfiles.get(0);
        GfxProfile gfxProfile = NesInfo.ntsc;
        EmulatorSettings settings = new EmulatorSettings();
        settings.historyEnabled = false;
        settings.loadSavFiles = false;
        settings.quality = 1;
        settings.saveSavFiles = false;
        settings.zapperEnabled = false;

        synchronized (lock) {
            bridge.start(gfxProfile.toInt(), sfxProfile.toInt(), settings.toInt());
            bridge.loadGame(file.getAbsolutePath(), file.getParent(), context.getCacheDir() + File.pathSeparator + "test.save");
            bridge.setViewPortSize(1920, 1080);
            bridge.initSound(sfxProfile);
            bridge.emulate(0, 0, 0);
        }

        updater = new EmulatorThread();
        updater.setFps(gfxProfile.fps);
        updater.start();

        if (audioEnabled) {
            audioPlayer = new AudioPlayer();
            audioPlayer.start();
        }
    }

    public void setBenchmark(Benchmark benchmark) {
        this.benchmark = benchmark;
    }

    public interface OnNotRespondingListener {
        void onNotResponding();
    }

    private class EmulatorThread extends Thread {

        private int totalSkipped;
        private long expectedTimeE1;
        private int exactDelayPerFrameE1;
        private long currentFrame;
        private long startTime;
        private boolean isPaused = true;
        private AtomicBoolean isRunning = new AtomicBoolean(true);
        private Object pauseLock = new Object();
        private int delayPerFrame;

        public void setFps(int fps) {
            exactDelayPerFrameE1 = (int) ((1000 / (float) fps) * 10);
            delayPerFrame = (int) (exactDelayPerFrameE1 / 10f + 0.5);
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
            setName("emudroid:gameLoop #" + (int) (Math.random() * 1000));
            Log.i(TAG, getName() + " started");
            long skippedTime = 0;
            totalSkipped = 0;
            unpause();
            expectedTimeE1 = 0;
            int cnt = 0;
            int afterSkip = 0;

            while (isRunning.get()) {
                if (benchmark != null) {
                    benchmark.notifyFrameEnd();
                }

                long time1 = System.currentTimeMillis();

                synchronized (pauseLock) {
                    while (isPaused) {
                        try {
                            pauseLock.wait();

                        } catch (InterruptedException ignored) {
                        }

                        if (benchmark != null) {
                            benchmark.reset();
                        }

                        time1 = System.currentTimeMillis();
                    }
                }

                int numFramesToSkip = 0;
                long realTime = (time1 - startTime);
                long diff = ((expectedTimeE1 / 10) - realTime);
                long delay = +diff;

                if (delay > 0) {
                    try {
                        Thread.sleep(delay);

                    } catch (Exception ignored) {
                    }
                } else {
                    try {
                        Thread.sleep(1);

                    } catch (Exception ignored) {
                    }
                }

                skippedTime = -diff;

                if (afterSkip > 0) {
                    afterSkip--;
                }

                if (skippedTime >= delayPerFrame * 3 && afterSkip == 0) {
                    numFramesToSkip = (int) (skippedTime / delayPerFrame) - 1;
                    int originalSkipped = numFramesToSkip;
                    numFramesToSkip = Math.min(originalSkipped, 8);
                    expectedTimeE1 += (numFramesToSkip * exactDelayPerFrameE1);
                    totalSkipped += numFramesToSkip;
                }

                if (benchmark != null) {
                    benchmark.notifyFrameStart();
                }

                synchronized (lock) {
                    if (true) {
                        JniBridge.getInstance().emulate(keys, 0, numFramesToSkip);
                        cnt += 1 + numFramesToSkip;

                        if (audioEnabled && cnt >= 3) {
                            JniBridge.getInstance().readSfxData();

                            synchronized (sfxReadyLock) {
                                sfxReady = true;
                                sfxReadyLock.notifyAll();
                            }

                            cnt = 0;
                        }
                    }
                }

                currentFrame += 1 + numFramesToSkip;
                expectedTimeE1 += exactDelayPerFrameE1;
            }

            Log.i(TAG, getName() + " finished");
        }

        public void unpause() {
            synchronized (pauseLock) {
                startTime = System.currentTimeMillis();
                currentFrame = 0;
                expectedTimeE1 = 0;
                isPaused = false;
                pauseLock.notifyAll();
            }
        }

        public void pause() {
            synchronized (pauseLock) {
                isPaused = true;
            }
        }

        public void destroy() {
            isRunning.set(false);
            unpause();
        }

    }

    private class AudioPlayer extends Thread {

        protected AtomicBoolean isRunning = new AtomicBoolean();

        @Override
        public void run() {
            isRunning.set(true);
            setName("emudroid:audioReader");

            while (isRunning.get()) {
                synchronized (sfxReadyLock) {
                    while (!sfxReady) {
                        try {
                            sfxReadyLock.wait();
                        } catch (Exception e) {
                            return;
                        }
                    }

                    sfxReady = false;
                }
                JniBridge.getInstance().renderSfx();
            }
        }

        public void destroy() {
            isRunning.set(false);
            this.interrupt();
        }


    }


}
