package com.My_peggle.ui;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
    private final SurfaceHolder surfaceHolder;
    private final CustomSurfaceView gameView;
    private boolean isRunning = false;

    // Physics constants
    private static final long NS_PER_SECOND = 1_000_000_000L;
    private static final long TARGET_FPS = 60;
    private static final long OPTIMAL_TIME = NS_PER_SECOND / TARGET_FPS;

    public GameThread(SurfaceHolder surfaceHolder, CustomSurfaceView gameView) {
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    @Override
    public void run() {
        long lastLoopTime = System.nanoTime();
        long lastFpsTime = 0;

        while (isRunning) {
            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;
            lastLoopTime = now;

            // Update game logic (Physics)
            // We use a fixed timestep approach or pass delta time
            gameView.update();

            // Render game
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    synchronized (surfaceHolder) {
                        gameView.draw(canvas);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Simple sleep to maintain target FPS and not burn CPU
            try {
                long sleepTime = (lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1_000_000;
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }
}
