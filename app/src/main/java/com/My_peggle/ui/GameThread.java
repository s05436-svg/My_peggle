package com.My_peggle.ui;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
public class GameThread extends Thread {
    private final SurfaceHolder surfaceHolder;
    private final com.My_peggle.ui.CustomSurfaceView gameView;
    private boolean isRunning = false;
    public GameThread(SurfaceHolder surfaceHolder, com.My_peggle.ui.CustomSurfaceView gameView) {
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    @Override
    public void run() {
        while (isRunning) {
            Canvas canvas = null;
            try {
                gameView.update();
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
        }
    }


}
