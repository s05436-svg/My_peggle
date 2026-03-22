package com.My_peggle.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.My_peggle.R;
import com.My_peggle.shapes.Ball;
import com.My_peggle.shapes.BaseShape;
import com.My_peggle.shapes.Cannon;
import com.My_peggle.shapes.Peg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;
    private final List<BaseShape> shapes = new ArrayList<>();
    private final List<Ball> balls = new ArrayList<>();
    private final List<Peg> pegs = new ArrayList<>();
    private boolean shapesInitialized = false;
    private Bitmap backgroundBitmap;
    private Bitmap ballContainerBitmap;
    private float ballContainerX;
    private float ballContainerY;
    private Cannon cannon;
    private int screenWidth;
    private int screenHeight;
    private long ballDeactivatedTime = 0;
    private List<Peg> pegsToClear = new ArrayList<>();
    private long pegClearStartTime = 0;
    private int pegClearIndex = 0;

    private int remainingBalls = 10;
    private Paint circlePaint;
    private Paint textPaint;

    public CustomSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
        initPaints();
    }

    private void initPaints() {
        circlePaint = new Paint();
        circlePaint.setColor(Color.BLUE);
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
    }

    private void initShapes(int viewWidth, int viewHeight) {
        // Load and scale the background
        Bitmap originalBackground = BitmapFactory.decodeResource(getResources(), R.drawable.peggle_background_new);
        if (originalBackground != null) {
            backgroundBitmap = Bitmap.createScaledBitmap(originalBackground, viewWidth, viewHeight, true);
        }

        // Load and position the ball container
        Bitmap originalBallContainer = BitmapFactory.decodeResource(getResources(), R.drawable.ball_container);
        if (originalBallContainer != null) {
            int containerHeight = 1250; // You can adjust the size
            int containerWidth = (int) (originalBallContainer.getWidth() * ((float) containerHeight / originalBallContainer.getHeight()));
            ballContainerBitmap = Bitmap.createScaledBitmap(originalBallContainer, containerWidth, containerHeight, true);
            // Position it at the bottom-center
            ballContainerX = ((viewWidth - containerWidth) / 4f) - 400f;
            ballContainerY = viewHeight - containerHeight + 30; // Adjust Y to make it sit nicely at the bottom
        }

        // Create the cannon at the top-center of the screen
        cannon = new Cannon(getContext(), viewWidth/2f, 100f, 700f, 350f);
        shapes.add(cannon);

        // Add 20 random pegs
        Random random = new Random();
        float gameAreaWidth = viewHeight;
        float gameLeft = (viewWidth - gameAreaWidth) / 2;
        float pegRadius = 30;

        float minX = gameLeft + pegRadius;
        float maxX = gameLeft + gameAreaWidth - pegRadius;
        float spawnWidth = maxX - minX;

        float minY = 300;
        float maxY = viewHeight - 500;
        float spawnHeight = maxY - minY;

        if (spawnWidth > 0 && spawnHeight > 0) {
            for (int i = 0; i < 20; i++) {
                float x = minX + random.nextFloat() * spawnWidth;
                float y = minY + random.nextFloat() * spawnHeight;

                Peg.PegType type = (random.nextFloat() > 0.3) ? Peg.PegType.BLUE : Peg.PegType.ORANGE; // 30% chance of being orange
                Peg peg = new Peg(getContext(), x, y, pegRadius, type);
                shapes.add(peg);
                pegs.add(peg);
            }
        }
    }

    public void update() {
        // Update all balls and remove if they are deactivated
        Iterator<Ball> ballIterator = balls.iterator();
        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();
            ball.update(screenWidth, screenHeight);
            if (ball.isDeactivated()) {
                remainingBalls--; // Decrement only after ball is deactivated
                pegsToClear.addAll(ball.getHitPegs());
                ballIterator.remove();
                ballDeactivatedTime = System.currentTimeMillis();
                pegClearStartTime = ballDeactivatedTime + 300;
                pegClearIndex = 0;
            }
        }

        if (!pegsToClear.isEmpty() && System.currentTimeMillis() >= pegClearStartTime) {
            if (pegClearIndex < pegsToClear.size()) {
                Peg pegToRemove = pegsToClear.get(pegClearIndex);
                pegs.remove(pegToRemove);
                shapes.remove(pegToRemove);
                pegClearIndex++;
                if (pegClearIndex < pegsToClear.size()) {
                    long delay = 700 / pegsToClear.size();
                    pegClearStartTime = System.currentTimeMillis() + delay;
                } else {
                    pegsToClear.clear();
                }
            }
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

                    // Reflect the ball's velocity and check if a hit occurred
                    if (ball.reflect(peg)) {
                        peg.hit();
                    }
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
        this.screenWidth = width;
        this.screenHeight = height;
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

        if (ballContainerBitmap != null) {
            canvas.drawBitmap(ballContainerBitmap, ballContainerX, ballContainerY, null);

            // Draw remaining balls counter
            float circleX = ballContainerX + ballContainerBitmap.getWidth() / 2f;
            float circleY = ballContainerY + 180;
            float radius = 50;

            canvas.drawCircle(circleX, circleY, radius, circlePaint);
            // Draw text centered in the circle
            float textY = circleY - ((textPaint.descent() + textPaint.ascent()) / 2);
            canvas.drawText(String.valueOf(remainingBalls), circleX, textY, textPaint);
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
            // Only fire a ball if there isn't one on the screen already, 1 second has passed, and we have balls left
            if (cannon != null && balls.isEmpty() && (System.currentTimeMillis() - ballDeactivatedTime > 1000) && remainingBalls > 0) {
                // Aim one last time to ensure it's perfectly aligned on click
                cannon.aim(event.getX(), event.getY());
                Ball newBall = cannon.fire(getContext());
                newBall.setTarget(event.getX(), event.getY());
                balls.add(newBall);
                // Removed remainingBalls-- from here
            }
        }
        return true;
    }
}
