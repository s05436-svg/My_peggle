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
import com.My_peggle.shapes.BaseShape;
import com.My_peggle.shapes.ImageShape;

import java.util.ArrayList;
import java.util.List;

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;
    private final List<BaseShape> shapes = new ArrayList<>();
    private ImageShape ballContainer; // Reference to the container
    private BaseShape selectedShape = null;
    private boolean shapesInitialized = false;
    private Bitmap backgroundBitmap;
    private int ballCount = 0; // Example variable to track balls

    public CustomSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    // Method to add a ball and update the container's appearance
    public void addBall() {
        ballCount++;
        // Assuming you have frames for 0, 1, 2, ... balls
        // This will crash if you add more balls than you have frames.
        if (ballContainer != null) {
            ballContainer.setFrame(ballCount);
        }
    }

    private void initShapes(int viewWidth, int viewHeight) {
        // Load and scale the background
        Bitmap originalBackground = BitmapFactory.decodeResource(getResources(), R.drawable.peggle_background_new);
        if (originalBackground != null) {
            backgroundBitmap = Bitmap.createScaledBitmap(originalBackground, viewWidth, viewHeight, true);
        }

        float containerWidth = 1200f;
        float containerHeight = 9 * viewHeight * 1.4f / 10;

        // Define all the frames for the container animation
        int[] containerFrames = {
                R.drawable.ball_container,      // Frame 0: Empty
                R.drawable.ball_container_1_ball, // Frame 1: One ball
                // Add more frames here, e.g., R.drawable.ball_container_2_balls
        };

        // Create a single container with all its potential frames
        ballContainer = new ImageShape(getContext(), 100, 500, containerWidth, containerHeight, false, containerFrames);
        shapes.add(ballContainer);
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

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                 // Example: Add a ball on touch
                 addBall();

                for (int i = shapes.size() - 1; i >= 0; i--) {
                    BaseShape shape = shapes.get(i);
                    if (shape.isTouched(x, y)) {
                        if (shape.isMovable()) {
                            selectedShape = shape;
                            break;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (selectedShape != null) {
                    selectedShape.setPosition(x, y);
                }
                break;

            case MotionEvent.ACTION_UP:
                selectedShape = null;
                break;
        }
        return true;
    }
}
