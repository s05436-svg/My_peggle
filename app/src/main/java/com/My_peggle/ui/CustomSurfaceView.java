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
import com.My_peggle.shapes.CircleShape;
import com.My_peggle.shapes.ImageShape;
import com.My_peggle.shapes.RectShape;
import com.My_peggle.shapes.TeleportCircle;
import com.My_peggle.shapes.TriangleShape;

import java.util.ArrayList;
import java.util.List;

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;
    private final List<BaseShape> shapes = new ArrayList<>();
    private BaseShape selectedShape = null;
    private TeleportCircle specialCircle;
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

        // 3 Rectangles
        shapes.add(new RectShape(200, 200, 200, 150, Color.RED));
        shapes.add(new RectShape(500, 200, 150, 150, Color.BLUE));
        shapes.add(new RectShape(800, 200, 100, 250, Color.GREEN));

        // 1 Normal Circle
        shapes.add(new CircleShape(300, 600, 80, Color.MAGENTA));

        // 1 Special Teleport Circle
        specialCircle = new TeleportCircle(700, 600, 100, Color.CYAN, Color.WHITE);
        shapes.add(specialCircle);

        // 1 Triangle
        shapes.add(new TriangleShape(500, 1000, 150, Color.YELLOW));

        float containerWidth = 450f;
        // Stretch the height to be 20% taller than the screen
        float containerHeight = viewHeight * 1.4f;
        ImageShape myBall = new ImageShape(getContext(), 80, 500, containerWidth, containerHeight, R.drawable.ball_container);
        myBall.setMovable(false);
        shapes.add(myBall);
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

        // Draw background image if available
        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        } else {
            // Background color fallback
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
                // Logic 1: Teleportation if active
                if (specialCircle.isActive) {
                    specialCircle.setPosition(x, y);
                    specialCircle.setActive(false);
                    return true;
                }

                // Logic 2: Hit detection
                for (int i = shapes.size() - 1; i >= 0; i--) {
                    BaseShape shape = shapes.get(i);
                    if (shape.isTouched(x, y)) {
                        if (shape == specialCircle) {
                            specialCircle.setActive(true);
                        } else {
                            selectedShape = shape;
                            // Bring to front
                            shapes.remove(i);
                            shapes.add(shape);
                        }
                        return true;
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
