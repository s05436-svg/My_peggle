package com.My_peggle.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.My_peggle.R;
import com.My_peggle.shapes.Ball;
import com.My_peggle.shapes.BaseShape;
import com.My_peggle.shapes.Peg;

import java.util.ArrayList;
import java.util.List;

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;
    private final List<BaseShape> shapes = new ArrayList<>();
    private boolean shapesInitialized = false;
    private Bitmap backgroundBitmap;

    public CustomSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    private void initShapes(int viewWidth, int viewHeight) {
        // Load and scale the background
        Bitmap originalBackground = BitmapFactory.decodeResource(getResources(), R.drawable.peggle_background_new);
        if (originalBackground != null) {
            backgroundBitmap = Bitmap.createScaledBitmap(originalBackground, viewWidth, viewHeight, true);
        }

        shapes.add(new Ball(getContext(), 500, 1000, 30));
        shapes.add(new Peg(getContext(), 500, 500, 30, Peg.PegType.BLUE));
        shapes.add(new Peg(getContext(), 700, 600, 30, Peg.PegType.ORANGE));
    }

    public void update() {
        for (BaseShape shape : shapes) {
            if (shape instanceof Ball) {
                ((Ball) shape).update();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gameThread = new GameThread(getHolder(), this);
        gameThread.setRunning(true);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!shapesInitialized) {
            initShapes(width, height);
            shapesInitialized = true;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas == null) return;

        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        } else {
            canvas.drawColor(Color.DKGRAY);
        }

        for (BaseShape shape : shapes) {
            shape.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (BaseShape shape : shapes) {
                if (shape instanceof Ball) {
                    ((Ball) shape).setTarget(x, y);
                }
            }
        }
        return true;
    }
}
