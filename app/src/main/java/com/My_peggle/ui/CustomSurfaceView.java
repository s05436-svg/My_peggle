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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.My_peggle.R;
import com.My_peggle.shapes.Ball;
import com.My_peggle.shapes.BaseShape;
import com.My_peggle.shapes.BottomHole;
import com.My_peggle.shapes.Cannon;
import com.My_peggle.shapes.Peg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;
    private final List<BaseShape> shapes = new ArrayList<>();
    private final List<Ball> balls = new ArrayList<>();
    private final List<Peg> pegs = new ArrayList<>();
    private final List<Peg> animatingPegs = new ArrayList<>();
    private final List<Ball> containerBalls = new ArrayList<>();
    private final List<Ball> animatingBalls = new ArrayList<>();
    private final List<Ball> enteringBalls = new ArrayList<>();
    private boolean shapesInitialized = false;
    private Bitmap backgroundBitmap;
    private Bitmap ballContainerBitmap;
    private float ballContainerX;
    private float ballContainerY;
    private Cannon cannon;
    private BottomHole bottomHole;
    private int screenWidth;
    private int screenHeight;
    private long ballDeactivatedTime = 0;
    private List<Peg> pegsToClear = new ArrayList<>();
    private long pegClearStartTime = 0;
    private int pegClearIndex = 0;

    private int remainingBalls = 10;
    private float containerBallRadius;
    private int totalScore = 0;
    private int currentShotScore = 0;
    private final int MAX_SCORE_BAR = 1500;

    private Paint circlePaint;
    private Paint textPaint;
    private Paint trajectoryPaint;
    private Paint barBackgroundPaint;
    private Paint barFillPaint;
    private Paint barTextPaint;
    private Paint glowPaint;
    
    private Ball activeBall; // Persistent ball object for reuse
    private Ball previewBall;
    private float lastTouchX, lastTouchY;

    // For stuck detection
    private float lastBallX, lastBallY;
    private int stuckFrames = 0;

    // Glow and Delay features
    private long glowStartTime = 0;
    private float glowX;
    private static final long GLOW_DURATION = 600;
    private long pendingBallEntryTime = 0;
    private boolean needsLoadingAfterEntry = false;

    // Grid Optimization
    private static final int CELL_SIZE = 150; 
    private List<Peg>[][] pegGrid;
    private int gridCols, gridRows;

    private List<Map<String, Object>> pendingLevelData;

    // Game Over Listener
    public interface OnGameOverListener {
        void onGameOver();
    }
    private OnGameOverListener gameOverListener;
    private boolean isGameOver = false;

    public CustomSurfaceView(Context context) {
        super(context);
        init();
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
        initPaints();
    }
    
    public void setGameOverListener(OnGameOverListener listener) {
        this.gameOverListener = listener;
    }

    public int getTotalScore() {
        return totalScore;
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

        glowPaint = new Paint();
        glowPaint.setColor(Color.YELLOW);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setAntiAlias(true);
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
            enteringBalls.clear();
            float startYOffset = 380f; 
            float bottomLimit = containerHeight - 225f;
            float availableHeight = bottomLimit - startYOffset;
            
            float verticalStep = availableHeight / 10f;
            containerBallRadius = (verticalStep / 2f) * 1.7f;
            float centerX = ballContainerX + containerWidth / 2f;

            for (int i = 0; i < remainingBalls - 1; i++) {
                float ballY = ballContainerY + bottomLimit - (i * verticalStep) - (verticalStep / 2f);
                containerBalls.add(new Ball(getContext(), centerX, ballY, containerBallRadius));
            }
        }

        cannon = new Cannon(getContext(), viewWidth/2f, 100f, 700f, 350f);
        shapes.add(cannon);

        float holeWidth = 280f;
        float holeHeight = 160f;
        bottomHole = new BottomHole(getContext(), viewWidth / 2f, viewHeight - 30, holeWidth, holeHeight);
        shapes.add(bottomHole);

        // Grid Initialization
        gridCols = (viewWidth / CELL_SIZE) + 1;
        gridRows = (viewHeight / CELL_SIZE) + 1;
        pegGrid = new ArrayList[gridCols][gridRows];
        for (int i = 0; i < gridCols; i++) {
            for (int j = 0; j < gridRows; j++) {
                pegGrid[i][j] = new ArrayList<>();
            }
        }

        // Create the persistent active ball off-screen initially
        activeBall = new Ball(getContext(), -1000, -1000, 15);

        if (pendingLevelData != null) {
            loadLevelFromCoordinates(pendingLevelData);
            pendingLevelData = null;
        }
        
        updatePegGrid();
    }

    public void setLevelData(List<Map<String, Object>> coordinates) {
        if (!shapesInitialized) {
            this.pendingLevelData = coordinates;
        } else {
            loadLevelFromCoordinates(coordinates);
        }
    }

    private void loadLevelFromCoordinates(List<Map<String, Object>> coordinates) {
        pegs.clear();
        // Keep only non-peg shapes (cannon, bottomHole)
        List<BaseShape> nonPegShapes = new ArrayList<>();
        for (BaseShape shape : shapes) {
            if (!(shape instanceof Peg)) {
                nonPegShapes.add(shape);
            }
        }
        shapes.clear();
        shapes.addAll(nonPegShapes);

        float pegRadius = 25f;
        Random random = new Random();

        for (Map<String, Object> coord : coordinates) {
            Object xObj = coord.get("x");
            Object yObj = coord.get("y");
            if (xObj != null && yObj != null) {
                float x = ((Number) xObj).floatValue();
                float y = ((Number) yObj).floatValue();

                // Determine type (random or from DB if exists)
                Peg.PegType type = (random.nextFloat() > 0.3) ? Peg.PegType.BLUE : Peg.PegType.ORANGE;
                if (coord.containsKey("type")) {
                    String typeStr = (String) coord.get("type");
                    if ("ORANGE".equals(typeStr)) type = Peg.PegType.ORANGE;
                    else if ("BLUE".equals(typeStr)) type = Peg.PegType.BLUE;
                }

                Peg peg = new Peg(getContext(), x, y, pegRadius, type);
                pegs.add(peg);
                shapes.add(peg);
            }
        }
        updatePegGrid();
    }

    private void updatePegGrid() {
        if (pegGrid == null) return;
        for (int i = 0; i < gridCols; i++) {
            for (int j = 0; j < gridRows; j++) {
                pegGrid[i][j].clear();
            }
        }
        for (Peg peg : pegs) {
            int col = (int) (peg.getX() / CELL_SIZE);
            int row = (int) (peg.getY() / CELL_SIZE);
            if (col >= 0 && col < gridCols && row >= 0 && row < gridRows) {
                pegGrid[col][row].add(peg);
            }
        }
    }

    public void update() {
        if (isGameOver) return;

        float gameAreaWidth = screenHeight;
        float gameLeft = (screenWidth - gameAreaWidth) / 2;
        float gameRight = gameLeft + gameAreaWidth;
        
        if (bottomHole != null) {
            bottomHole.update(gameLeft, gameRight);
        }

        if (activeBall != null && activeBall.isMoving()) {
            activeBall.update(screenWidth, screenHeight);
            
            float distMoved = (float) Math.sqrt(Math.pow(activeBall.getX() - lastBallX, 2) + Math.pow(activeBall.getY() - lastBallY, 2));
            if (distMoved < 2.0f) {
                stuckFrames++;
            } else {
                stuckFrames = 0;
            }
            lastBallX = activeBall.getX();
            lastBallY = activeBall.getY();

            if (stuckFrames > 30) {
                Peg lowestPeg = findPegNearby(activeBall.getX(), activeBall.getY(), (activeBall.getRadius() + 25) * 1.5f);
                if (lowestPeg != null) {
                    removePeg(lowestPeg);
                    stuckFrames = 0;
                }
            }

            boolean caughtInHole = false;
            if (bottomHole != null) {
                float dx = activeBall.getX() - bottomHole.getX();
                if (Math.abs(dx) < bottomHole.getWidth() / 2 + activeBall.getRadius() && 
                    activeBall.getY() > bottomHole.getY() - bottomHole.getHeight() / 2) {
                    caughtInHole = true;
                }
            }

            if (activeBall.isDeactivated() || caughtInHole) {
                if (caughtInHole) {
                    glowStartTime = System.currentTimeMillis();
                    glowX = bottomHole.getX();
                    pendingBallEntryTime = System.currentTimeMillis() + 500;
                    needsLoadingAfterEntry = true;
                } else {
                    triggerLoadingAnimation();
                }
                
                totalScore += currentShotScore;
                currentShotScore = 0;
                stuckFrames = 0;

                pegsToClear.addAll(activeBall.getHitPegs());
                activeBall.reset(-2000, -2000); // Stop movement and move off-screen
                ballDeactivatedTime = System.currentTimeMillis();
                pegClearStartTime = ballDeactivatedTime + 300;
                pegClearIndex = 0;
                
                // Check for Game Over after ball is deactivated
                if (remainingBalls == 0 && enteringBalls.isEmpty() && animatingBalls.isEmpty()) {
                    isGameOver = true;
                    if (gameOverListener != null) {
                        gameOverListener.onGameOver();
                    }
                }
            }
            
            checkCollisions();
        }

        if (pendingBallEntryTime != 0 && System.currentTimeMillis() >= pendingBallEntryTime) {
            float centerX = ballContainerX + (ballContainerBitmap != null ? ballContainerBitmap.getWidth() / 2f : 0);
            float startY = ballContainerY + 280;
            Ball enteringBall = new Ball(getContext(), centerX, startY, containerBallRadius);
            enteringBalls.add(enteringBall);
            pendingBallEntryTime = 0;
        }

        float counterY = ballContainerY + 280;
        Iterator<Ball> animIterator = animatingBalls.iterator();
        while (animIterator.hasNext()) {
            Ball b = animIterator.next();
            float newY = b.getY() - 30; 
            if (newY <= counterY) {
                animIterator.remove();
            } else {
                b.setPosition(b.getX(), newY);
            }
        }

        Iterator<Ball> enterIterator = enteringBalls.iterator();
        while (enterIterator.hasNext()) {
            Ball b = enterIterator.next();
            float containerHeight = 1450;
            float startYOffset = 380f; 
            float bottomLimit = containerHeight - 225f;
            float availableHeight = bottomLimit - startYOffset;
            float verticalStep = availableHeight / 10f;
            float targetY = ballContainerY + bottomLimit - (containerBalls.size() * verticalStep) - (verticalStep / 2f);
            
            float newY = b.getY() + 30;
            if (newY >= targetY) {
                b.setPosition(b.getX(), targetY);
                containerBalls.add(b);
                remainingBalls++; 
                enterIterator.remove();
                if (enteringBalls.isEmpty() && needsLoadingAfterEntry) {
                    triggerLoadingAnimation();
                    needsLoadingAfterEntry = false;
                }
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
                removePeg(pegToRemove);
                pegClearIndex++;
                if (pegClearIndex < pegsToClear.size()) {
                    long delay = 700 / pegsToClear.size();
                    pegClearStartTime = System.currentTimeMillis() + delay;
                } else {
                    pegsToClear.clear();
                }
            }
        }
    }

    private void removePeg(Peg peg) {
        if (pegs.contains(peg)) {
            pegs.remove(peg);
            shapes.remove(peg);
            peg.startAnimation();
            animatingPegs.add(peg);
            updatePegGrid();
        }
    }

    private Peg findPegNearby(float x, float y, float radius) {
        if (pegGrid == null) return null;
        int col = (int) (x / CELL_SIZE);
        int row = (int) (y / CELL_SIZE);
        Peg lowestPeg = null;
        float maxPegY = -1;

        for (int i = col - 1; i <= col + 1; i++) {
            for (int j = row - 1; j <= row + 1; j++) {
                if (i >= 0 && i < gridCols && j >= 0 && j < gridRows) {
                    for (Peg peg : pegGrid[i][j]) {
                        float dx = x - peg.getX();
                        float dy = y - peg.getY();
                        float dist = (float) Math.sqrt(dx * dx + dy * dy);
                        if (dist < radius) {
                            if (peg.getY() > maxPegY) {
                                maxPegY = peg.getY();
                                lowestPeg = peg;
                            }
                        }
                    }
                }
            }
        }
        return lowestPeg;
    }

    private void triggerLoadingAnimation() {
        if (remainingBalls > 0) {
            remainingBalls--;
            if (!containerBalls.isEmpty()) {
                Ball topBall = containerBalls.remove(containerBalls.size() - 1);
                animatingBalls.add(topBall);
            }
        }
    }

    private void checkCollisions() {
        if (activeBall == null || !activeBall.isMoving() || pegGrid == null) return;
        
        int col = (int) (activeBall.getX() / CELL_SIZE);
        int row = (int) (activeBall.getY() / CELL_SIZE);

        for (int i = col - 1; i <= col + 1; i++) {
            for (int j = row - 1; j <= row + 1; j++) {
                if (i >= 0 && i < gridCols && j >= 0 && j < gridRows) {
                    for (Peg peg : pegGrid[i][j]) {
                        float dx = activeBall.getX() - peg.getX();
                        float dy = activeBall.getY() - peg.getY();
                        float distance = (float) Math.sqrt(dx * dx + dy * dy);
                        float effectiveBallRadius = activeBall.getRadius() * 0.15f; 
                        float sumOfRadii = effectiveBallRadius + peg.getRadius();
                        if (distance < sumOfRadii) {
                            float overlap = sumOfRadii - distance;
                            float newX = activeBall.getX() + overlap * (dx / distance);
                            float newY = activeBall.getY() + overlap * (dy / distance);
                            activeBall.setPosition(newX, newY);
                            if (activeBall.reflect(peg)) {
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

        drawHoleGlow(canvas);

        if (ballContainerBitmap != null) {
            canvas.drawBitmap(ballContainerBitmap, ballContainerX, ballContainerY, null);
            for (Ball b : containerBalls) b.draw(canvas);
            for (Ball b : animatingBalls) b.draw(canvas);
            for (Ball b : enteringBalls) b.draw(canvas);

            float circleX = ballContainerX + ballContainerBitmap.getWidth() / 2f;
            float circleY = ballContainerY + 280;
            float radius = 50;
            canvas.drawCircle(circleX, circleY, radius, circlePaint);
            float textY = circleY - ((textPaint.descent() + textPaint.ascent()) / 2);
            canvas.drawText(String.valueOf(remainingBalls), circleX, textY, textPaint);
        }

        drawScoreBar(canvas);
        for (BaseShape shape : shapes) shape.draw(canvas);
        
        if (previewBall != null) {
            drawTrajectory(canvas);
            previewBall.draw(canvas);
        }
        
        for (Peg p : animatingPegs) p.draw(canvas);
        if (activeBall != null && activeBall.isMoving()) activeBall.draw(canvas);
    }

    private void drawHoleGlow(Canvas canvas) {
        if (System.currentTimeMillis() - glowStartTime < GLOW_DURATION) {
            long elapsed = System.currentTimeMillis() - glowStartTime;
            float ratio = (float) elapsed / GLOW_DURATION;
            int alpha = (int) (180 * (1.0f - ratio));
            glowPaint.setAlpha(alpha);
            float scale = 1.0f + ratio * 0.8f; 
            float w = 300 * scale;
            float h = 150 * scale;
            RectF glowRect = new RectF(glowX - w/2, screenHeight - h, glowX + w/2, screenHeight + 50);
            canvas.drawOval(glowRect, glowPaint);
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
        if (previewBall == null || cannon == null || pegGrid == null) return;
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
                
                int col = (int) (simX / CELL_SIZE);
                int row = (int) (simY / CELL_SIZE);

                outerLoop:
                for (int m = col - 1; m <= col + 1; m++) {
                    for (int n = row - 1; n <= row + 1; n++) {
                        if (m >= 0 && m < gridCols && n >= 0 && n < gridRows) {
                            for (Peg peg : pegGrid[m][n]) {
                                float pdx = simX - peg.getX();
                                float pdy = simY - peg.getY();
                                float dist = (float) Math.sqrt(pdx * pdx + pdy * pdy);
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
                                    break outerLoop;
                                }
                            }
                        }
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
        if (isGameOver) return true;

        float x = event.getX();
        float y = event.getY();
        lastTouchX = x;
        lastTouchY = y;
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (cannon != null && (activeBall == null || !activeBall.isMoving()) && (System.currentTimeMillis() - ballDeactivatedTime > 1000) && remainingBalls > 0) {
                    cannon.aim(x, y);
                    if (previewBall == null) {
                        previewBall = new Ball(getContext(), cannon.getNozzleX(), cannon.getNozzleY(), 15);
                    } else {
                        previewBall.reset(cannon.getNozzleX(), cannon.getNozzleY());
                    }
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
                    activeBall.reset(cannon.getNozzleX(), cannon.getNozzleY());
                    activeBall.setTarget(x, y);
                    activeBall.setMoving(true);
                    previewBall = null;
                }
                break;
        }
        return true;
    }
}
