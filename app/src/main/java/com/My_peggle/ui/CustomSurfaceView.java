package com.My_peggle.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
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
    private final List<Peg> animatingPegs = new ArrayList<>();
    private final List<Ball> containerBalls = new ArrayList<>();
    private final List<Ball> animatingBalls = new ArrayList<>();
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
    private Paint trajectoryPaint;
    
    private Ball previewBall;
    private float lastTouchX, lastTouchY;

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

        trajectoryPaint = new Paint();
        trajectoryPaint.setColor(Color.WHITE);
        trajectoryPaint.setStyle(Paint.Style.STROKE);
        trajectoryPaint.setStrokeWidth(8f);
        trajectoryPaint.setAntiAlias(true);
        trajectoryPaint.setAlpha(150);
        // Use a dashed line effect for the arrow/path
        trajectoryPaint.setPathEffect(new DashPathEffect(new float[]{20, 20}, 0));
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
            float ballDiameter = 30f; // Ball radius is 15
            int containerWidth = (int) (ballDiameter * 18.0f); 
            int containerHeight = 1450;
            
            ballContainerBitmap = Bitmap.createScaledBitmap(originalBallContainer, containerWidth, containerHeight, true);
            ballContainerX = ((viewWidth - containerWidth) / 4f) - 130f;
            ballContainerY = viewHeight - containerHeight + 200;

            // Initialize balls inside the container
            containerBalls.clear();
            animatingBalls.clear();
            float startYOffset = 380f; // Below the counter
            float bottomLimit = containerHeight - 225f;
            float availableHeight = bottomLimit - startYOffset;
            
            float verticalStep = availableHeight / 10f;
            float cBallRadius = (verticalStep / 2f) * 1.5f;
            float centerX = ballContainerX + containerWidth / 2f;

            for (int i = 0; i < 9; i++) {
                float ballY = ballContainerY + bottomLimit - (i * verticalStep) - (verticalStep / 2f);
                containerBalls.add(new Ball(getContext(), centerX, ballY, cBallRadius));
            }
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

                Peg.PegType type = (random.nextFloat() > 0.3) ? Peg.PegType.BLUE : Peg.PegType.ORANGE; 
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
                remainingBalls--; 
                if (!containerBalls.isEmpty()) {
                    Ball topBall = containerBalls.remove(containerBalls.size() - 1);
                    animatingBalls.add(topBall);
                }
                pegsToClear.addAll(ball.getHitPegs());
                ballIterator.remove();
                ballDeactivatedTime = System.currentTimeMillis();
                pegClearStartTime = ballDeactivatedTime + 300;
                pegClearIndex = 0;
            }
        }

        // Animation logic for sucking balls up
        float counterY = ballContainerY + 280;
        Iterator<Ball> animIterator = animatingBalls.iterator();
        while (animIterator.hasNext()) {
            Ball b = animIterator.next();
            float newY = b.getY() - 40; 
            if (newY <= counterY) {
                animIterator.remove();
            } else {
                b.setPosition(b.getX(), newY);
            }
        }
        
        // Peg fade animation logic
        Iterator<Peg> pegAnimIterator = animatingPegs.iterator();
        while (pegAnimIterator.hasNext()) {
            Peg p = pegAnimIterator.next();
            if (p.updateAnimation()) {
                pegAnimIterator.remove();
            }
        }

        if (!pegsToClear.isEmpty() && System.currentTimeMillis() >= pegClearStartTime) {
            if (pegClearIndex < pegsToClear.size()) {
                Peg pegToRemove = pegsToClear.get(pegClearIndex);
                pegs.remove(pegToRemove);
                shapes.remove(pegToRemove);
                
                // Start fade animation instead of just deleting
                pegToRemove.startAnimation();
                animatingPegs.add(pegToRemove);
                
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
                    float overlap = sumOfRadii - distance;
                    float newX = ball.getX() + overlap * (dx / distance);
                    float newY = ball.getY() + overlap * (dy / distance);
                    ball.setPosition(newX, newY);

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

            for (Ball b : containerBalls) {
                b.draw(canvas);
            }

            for (Ball b : animatingBalls) {
                b.draw(canvas);
            }

            float circleX = ballContainerX + ballContainerBitmap.getWidth() / 2f;
            float circleY = ballContainerY + 280;
            float radius = 50;

            canvas.drawCircle(circleX, circleY, radius, circlePaint);
            float textY = circleY - ((textPaint.descent() + textPaint.ascent()) / 2);
            canvas.drawText(String.valueOf(remainingBalls), circleX, textY, textPaint);
        }

        for (BaseShape shape : shapes) {
            shape.draw(canvas);
        }
        
        // Draw the preview ball and trajectory inside cannon if it exists
        if (previewBall != null) {
            drawTrajectory(canvas);
            previewBall.draw(canvas);
        }
        
        // Draw the animating (fading) pegs
        for (Peg p : animatingPegs) {
            p.draw(canvas);
        }

         for (Ball ball : balls) {
            ball.draw(canvas);
        }
    }

    private void drawTrajectory(Canvas canvas) {
        if (previewBall == null || cannon == null) return;

        float startX = previewBall.getX();
        float startY = previewBall.getY();
        float ballRadius = previewBall.getRadius();
        
        float dx = lastTouchX - startX;
        float dy = lastTouchY - startY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            float vX = (dx / distance) * Ball.SPEED;
            float vY = (dy / distance) * Ball.SPEED;
            
            Path path = new Path();
            float simX = startX;
            float simY = startY;
            
            // Start the arrow a bit after the ball
            float offsetSteps = 2; 
            for(int i=0; i<offsetSteps; i++) {
                vY += Ball.GRAVITY_ACCELERATION * Ball.TIME_STEP;
                simX += vX * Ball.TIME_STEP;
                simY += vY * Ball.TIME_STEP;
            }
            
            path.moveTo(simX, simY);

            // Simulation loop with accurate intersection to prevent "bending"
            for (int i = 0; i < 11; i++) {
                float prevSimX = simX;
                float prevSimY = simY;
                
                vY += Ball.GRAVITY_ACCELERATION * Ball.TIME_STEP;
                simX += vX * Ball.TIME_STEP;
                simY += vY * Ball.TIME_STEP;

                boolean hit = false;
                for (Peg peg : pegs) {
                    float pdx = simX - peg.getX();
                    float pdy = simY - peg.getY();
                    float dist = (float) Math.sqrt(pdx * pdx + pdy * pdy);
                    float rSum = ballRadius + peg.getRadius();
                    
                    if (dist < rSum) {
                        float prevPdx = prevSimX - peg.getX();
                        float prevPdy = prevSimY - peg.getY();
                        float prevDist = (float) Math.sqrt(prevPdx * prevPdx + prevPdy * prevPdy);
                        
                        // Linear interpolation finds the point on the existing straight line
                        if (prevDist > rSum && Math.abs(dist - prevDist) > 0.001f) {
                            float t = (rSum - prevDist) / (dist - prevDist);
                            simX = prevSimX + t * (simX - prevSimX);
                            simY = prevSimY + t * (simY - prevSimY);
                        }
                        hit = true;
                        break;
                    }
                }

                path.lineTo(simX, simY);
                if (hit) break;
            }
            
            canvas.drawPath(path, trajectoryPaint);
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
            if (cannon != null && previewBall == null) {
                cannon.aim(event.getX(), event.getY());
            }
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        lastTouchX = x;
        lastTouchY = y;
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (cannon != null && balls.isEmpty() && (System.currentTimeMillis() - ballDeactivatedTime > 1000) && remainingBalls > 0) {
                    cannon.aim(x, y);
                    previewBall = cannon.fire(getContext());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (cannon != null && previewBall != null) {
                    cannon.aim(x, y);
                    previewBall.setPosition(cannon.getNozzleX(), cannon.getNozzleY());
                }
                break;
            case MotionEvent.ACTION_UP:
                if (cannon != null && previewBall != null) {
                    cannon.aim(x, y);
                    previewBall.setPosition(cannon.getNozzleX(), cannon.getNozzleY());
                    previewBall.setTarget(x, y);
                    balls.add(previewBall);
                    previewBall = null;
                }
                break;
        }
        return true;
    }
}
