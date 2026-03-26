package com.My_peggle.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
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
    private int totalScore = 0;
    private int currentShotScore = 0;
    private final int MAX_SCORE_BAR = 1500;

    private Paint circlePaint;
    private Paint textPaint;
    private Paint trajectoryPaint;
    private Paint barBackgroundPaint;
    private Paint barFillPaint;
    private Paint barTextPaint;
    
    private Ball previewBall;
    private float lastTouchX, lastTouchY;

    // For stuck detection
    private float lastBallX, lastBallY;
    private int stuckFrames = 0;

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
        trajectoryPaint.setPathEffect(new DashPathEffect(new float[]{20, 20}, 0));

        barBackgroundPaint = new Paint();
        barBackgroundPaint.setColor(Color.DKGRAY);
        barBackgroundPaint.setAlpha(180);
        barBackgroundPaint.setStyle(Paint.Style.FILL);

        barFillPaint = new Paint();
        barFillPaint.setColor(Color.rgb(255, 165, 0)); // Orange/Gold for the score
        barFillPaint.setStyle(Paint.Style.FILL);

        barTextPaint = new Paint();
        barTextPaint.setColor(Color.WHITE);
        barTextPaint.setTextSize(35);
        barTextPaint.setTextAlign(Paint.Align.CENTER);
        barTextPaint.setFakeBoldText(true);
        barTextPaint.setAntiAlias(true);
    }

    private void initShapes(int viewWidth, int viewHeight) {
        Bitmap originalBackground = BitmapFactory.decodeResource(getResources(), R.drawable.peggle_background_new);
        if (originalBackground != null) {
            backgroundBitmap = Bitmap.createScaledBitmap(originalBackground, viewWidth, viewHeight, true);
        }

        Bitmap originalBallContainer = BitmapFactory.decodeResource(getResources(), R.drawable.ball_container);
        if (originalBallContainer != null) {
            float ballDiameter = 30f; 
            int containerWidth = (int) (ballDiameter * 18.0f); 
            int containerHeight = 1450;
            
            ballContainerBitmap = Bitmap.createScaledBitmap(originalBallContainer, containerWidth, containerHeight, true);
            ballContainerX = ((viewWidth - containerWidth) / 4f) - 130f;
            ballContainerY = viewHeight - containerHeight + 200;

            containerBalls.clear();
            animatingBalls.clear();
            float startYOffset = 380f; 
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

        cannon = new Cannon(getContext(), viewWidth/2f, 100f, 700f, 350f);
        shapes.add(cannon);

        createButterflyPattern(viewWidth, viewHeight);
    }

    private void createButterflyPattern(int viewWidth, int viewHeight) {
        float centerX = viewWidth / 2f;
        float centerY = viewHeight / 2f + 80f;
        float scale = 140f; 
        float pegRadius = 25f;
        float minSpacing = pegRadius * 2.5f; 
        Random random = new Random();

        float lastX = -10000, lastY = -10000;

        for (float t = 0; t < 2 * Math.PI; t += 0.01f) {
            double multiplier = Math.exp(Math.cos(t)) - 2 * Math.cos(4 * t) - Math.pow(Math.sin(t / 12), 5);
            float x = (float) (Math.sin(t) * multiplier) * scale;
            float y = (float) (-Math.cos(t) * multiplier) * scale;

            float dist = (float) Math.sqrt(Math.pow(x - lastX, 2) + Math.pow(y - lastY, 2));

            if (dist >= minSpacing) {
                float finalX = centerX + x;
                float finalY = centerY + y;

                Peg.PegType type = (random.nextFloat() > 0.3) ? Peg.PegType.BLUE : Peg.PegType.ORANGE;
                Peg peg = new Peg(getContext(), finalX, finalY, pegRadius, type);
                pegs.add(peg);
                shapes.add(peg);

                lastX = x;
                lastY = y;
            }
        }

        float bodyHeight = scale * 1.2f;
        for (float yOffset = -bodyHeight / 2; yOffset <= bodyHeight / 2; yOffset += minSpacing) {
            boolean overlap = false;
            for (Peg p : pegs) {
                float d = (float) Math.sqrt(Math.pow(centerX - p.getX(), 2) + Math.pow(centerY + yOffset - p.getY(), 2));
                if (d < minSpacing * 0.8f) {
                    overlap = true;
                    break;
                }
            }
            if (!overlap) {
                Peg peg = new Peg(getContext(), centerX, centerY + yOffset, pegRadius, Peg.PegType.ORANGE);
                pegs.add(peg);
                shapes.add(peg);
            }
        }
    }

    public void update() {
        Iterator<Ball> ballIterator = balls.iterator();
        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();
            ball.update(screenWidth, screenHeight);
            
            float distMoved = (float) Math.sqrt(Math.pow(ball.getX() - lastBallX, 2) + Math.pow(ball.getY() - lastBallY, 2));
            if (distMoved < 2.0f) {
                stuckFrames++;
            } else {
                stuckFrames = 0;
            }
            lastBallX = ball.getX();
            lastBallY = ball.getY();

            if (stuckFrames > 30) {
                Peg lowestPeg = null;
                float maxPegY = -1;
                
                for (Peg peg : pegs) {
                    float dx = ball.getX() - peg.getX();
                    float dy = ball.getY() - peg.getY();
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist < (ball.getRadius() + peg.getRadius()) * 1.5f) {
                        if (peg.getY() > maxPegY) {
                            maxPegY = peg.getY();
                            lowestPeg = peg;
                        }
                    }
                }
                
                if (lowestPeg != null) {
                    pegs.remove(lowestPeg);
                    shapes.remove(lowestPeg);
                    lowestPeg.startAnimation();
                    animatingPegs.add(lowestPeg);
                    stuckFrames = 0;
                }
            }

            if (ball.isDeactivated()) {
                remainingBalls--; 
                totalScore += currentShotScore;
                currentShotScore = 0;
                stuckFrames = 0;

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

        checkCollisions();
    }

    private void checkCollisions() {
        for (Ball ball : balls) {
            for (Peg peg : pegs) {
                float dx = ball.getX() - peg.getX();
                float dy = ball.getY() - peg.getY();
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                // GREATLY reduced effective radius to make ball touch the peg image
                float effectiveBallRadius = ball.getRadius() * 0.15f; 
                float sumOfRadii = effectiveBallRadius + peg.getRadius();

                if (distance < sumOfRadii) {
                    float overlap = sumOfRadii - distance;
                    float newX = ball.getX() + overlap * (dx / distance);
                    float newY = ball.getY() + overlap * (dy / distance);
                    ball.setPosition(newX, newY);

                    if (ball.reflect(peg)) {
                        if (!peg.isHit()) {
                            if (peg.getType() == Peg.PegType.ORANGE) {
                                currentShotScore += 100;
                            } else {
                                currentShotScore += 10;
                            }
                            peg.hit();
                        }
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

        drawScoreBar(canvas);

        for (BaseShape shape : shapes) {
            shape.draw(canvas);
        }
        
        if (previewBall != null) {
            drawTrajectory(canvas);
            previewBall.draw(canvas);
        }
        
        for (Peg p : animatingPegs) {
            p.draw(canvas);
        }

         for (Ball ball : balls) {
            ball.draw(canvas);
        }
    }

    private void drawScoreBar(Canvas canvas) {
        float barWidth = 60f;
        float barHeight = screenHeight * 0.6f;
        float xPos = screenWidth - barWidth - 60f;
        float yPos = (screenHeight - barHeight) / 2f;

        RectF bgRect = new RectF(xPos, yPos, xPos + barWidth, yPos + barHeight);
        canvas.drawRoundRect(bgRect, 15, 15, barBackgroundPaint);

        int displayScore = Math.min(totalScore + currentShotScore, MAX_SCORE_BAR);
        float fillHeight = (displayScore / (float) MAX_SCORE_BAR) * barHeight;

        RectF fillRect = new RectF(xPos, yPos + barHeight - fillHeight, xPos + barWidth, yPos + barHeight);
        canvas.drawRoundRect(fillRect, 15, 15, barFillPaint);

        canvas.drawText("1500", xPos + barWidth / 2, yPos - 20, barTextPaint);
        canvas.drawText(String.valueOf(displayScore), xPos + barWidth / 2, yPos + barHeight + 40, barTextPaint);
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
            
            float offsetSteps = 2; 
            for(int i=0; i<offsetSteps; i++) {
                vY += Ball.GRAVITY_ACCELERATION * Ball.TIME_STEP;
                simX += vX * Ball.TIME_STEP;
                simY += vY * Ball.TIME_STEP;
            }
            
            path.moveTo(simX, simY);

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
                    
                    // Reduced radius in trajectory to match logic
                    float effectiveBallRadius = ballRadius * 0.15f;
                    float rSum = effectiveBallRadius + peg.getRadius();
                    
                    if (dist < rSum) {
                        float prevPdx = prevSimX - peg.getX();
                        float prevPdy = prevSimY - peg.getY();
                        float prevDist = (float) Math.sqrt(prevPdx * prevPdx + prevPdy * prevPdy);
                        
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
                    currentShotScore = 0;
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
