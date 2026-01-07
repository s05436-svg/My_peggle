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
import com.My_peggle.shapes.Cannon;
import com.My_peggle.shapes.Peg;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;
    private final List<BaseShape> shapes = new ArrayList<>();
    private final List<Ball> balls = new ArrayList<>();
    private final List<Peg> pegs = new ArrayList<>();
    private boolean shapesInitialized = false;
    private Bitmap backgroundBitmap;
    private Cannon cannon;

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

        // Create the cannon at the top-center of the screen
        cannon = new Cannon(getContext(), viewWidth/2f, 100f, 700f, 350f);
        shapes.add(cannon);

        // Add some pegs
        Peg peg1 = new Peg(getContext(), 500, 500, 30, Peg.PegType.BLUE);
        Peg peg2 = new Peg(getContext(), 700, 600, 30, Peg.PegType.ORANGE);
        shapes.add(peg1);
        shapes.add(peg2);
        pegs.add(peg1);
        pegs.add(peg2);
    }

    public void update() {
        // Update all balls
        for (Ball ball : balls) {
            ball.update();
        }

        // Check for collisions
        checkCollisions();
    }

    private void checkCollisions() {
        for (Ball ball : balls) {
            for (Peg peg : pegs) {
                float dx = ball.getX() - peg.getX();
                float dy = ball.getY() - peg.getY();
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                float sumOfRadii = ball.getRadius() + peg.getRadius();

                if (distance < sumOfRadii) {
                    // Collision detected

                    // Reposition the ball to the point of contact to prevent sticking
                    float overlap = sumOfRadii - distance;
                    float newX = ball.getX() + overlap * (dx / distance);
                    float newY = ball.getY() + overlap * (dy / distance);
                    ball.setPosition(newX, newY);

                    // Reflect the ball's velocity
                    ball.reflect(peg);
                }
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
         for (Ball ball : balls) {
            ball.draw(canvas);
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
            if (cannon != null) {
                cannon.aim(event.getX(), event.getY());
            }
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (cannon != null) {
                // Aim one last time to ensure it's perfectly aligned on click
                cannon.aim(event.getX(), event.getY());
                Ball newBall = cannon.fire(getContext());
                newBall.setTarget(event.getX(), event.getY());
                balls.add(newBall);
            }
        }
        return true;
    }
}
