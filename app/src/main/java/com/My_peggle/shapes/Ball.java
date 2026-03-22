package com.My_peggle.shapes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

import com.My_peggle.R;

import java.util.ArrayList;
import java.util.List;

public class Ball extends BaseShape {

    private float velocityX;
    private float velocityY;
    private float radius;
    private Bitmap bitmap;
    public static final float SPEED = 55.0f; 
    private boolean isMoving = false;
    public static final float GRAVITY_ACCELERATION = 6.0f; 
    public static final float TIME_STEP = 0.37f;
    private static final float DAMPING_FACTOR = 0.8f; 
    private boolean isDeactivated = false;
    private List<Peg> hitPegs = new ArrayList<>();


    private static final String TAG = "Ball";

    public Ball(Context context, float x, float y, float radius) {
        super(x, y);
        this.radius = radius;
        this.velocityX = 0;
        this.velocityY = 0;
        setMovable(true);

        int resId = R.drawable.silver_ball;

        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        if (originalBitmap != null) {
            this.bitmap = Bitmap.createScaledBitmap(originalBitmap, (int) (radius * 2), (int) (radius * 2), true);
        }
    }

    public List<Peg> getHitPegs() {
        return hitPegs;
    }

    public float getRadius() {
        return radius;
    }
    public float getX() { 
        return x;
    }

    public float getY() { 
        return y;
    }

    public void setTarget(float targetX, float targetY) {
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            velocityX = (dx / distance) * SPEED;
            velocityY = (dy / distance) * SPEED;
            isMoving = true;
        }
    }

    public boolean reflect(Peg peg) {
        float normalX = x - peg.getX();
        float normalY = y - peg.getY();
        float distance = (float) Math.sqrt(normalX * normalX + normalY * normalY);

        if (distance == 0) return false;

        float unitNormalX = normalX / distance;
        float unitNormalY = normalY / distance;

        float dotProduct = velocityX * unitNormalX + velocityY * unitNormalY;

        if (dotProduct >= 0) {
            return false;
        }

        if (!hitPegs.contains(peg)) {
            hitPegs.add(peg);
        }

        velocityX = velocityX - 2 * dotProduct * unitNormalX;
        velocityY = velocityY - 2 * dotProduct * unitNormalY;

        if (y > peg.getY()) {
            velocityX *= DAMPING_FACTOR;
            velocityY *= DAMPING_FACTOR;
        }

        return true;
    }

    public void update(int screenWidth, int screenHeight) {
        if (isMoving) {
            velocityY += GRAVITY_ACCELERATION * TIME_STEP;

            x += velocityX * TIME_STEP;
            y += velocityY * TIME_STEP;

            float gameAreaWidth = screenHeight;
            float gameLeft = (screenWidth - gameAreaWidth) / 2;
            float gameRight = gameLeft + gameAreaWidth;

            if (x - radius < gameLeft) {
                x = gameLeft + radius;
                velocityX *= -1;
            } else if (x + radius > gameRight) {
                x = gameRight - radius;
                velocityX *= -1;
            }

            if (y - radius < 0) { 
                y = radius;
                velocityY *= -1;
            }

            if (y - radius > screenHeight) {
                deactivate();
            }
        }
    }

    public boolean isDeactivated() {
        return isDeactivated;
    }

    public void deactivate() {
        isDeactivated = true;
    }

    @Override
    public void draw(Canvas canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, x - radius, y - radius, null);
        }
    }

    @Override
    public boolean isTouched(float touchX, float touchY) {
        float distance = (float) Math.sqrt(Math.pow(touchX - x, 2) + Math.pow(touchY - y, 2));
        return distance <= radius;
    }
}
