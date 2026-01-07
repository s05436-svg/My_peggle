package com.My_peggle.shapes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

import com.My_peggle.R;

public class Ball extends BaseShape {

    private float velocityX;
    private float velocityY;
    private float radius;
    private Bitmap bitmap;
    private final float speed = 40.0f; // Constant speed for the ball
    private boolean isMoving = false;
    private final float gravity = 0.2f; // Constant downward force

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

    public float getRadius() {
        return radius;
    }
    public float getX() { // Getter for x coordinate
        return x;
    }

    public float getY() { // Getter for y coordinate
        return y;
    }

    public void setTarget(float targetX, float targetY) {
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            velocityX = (dx / distance) * speed;
            velocityY = (dy / distance) * speed;
            isMoving = true;
        }
    }

    public void reflect(Peg peg) {
        float normalX = x - peg.getX();
        float normalY = y - peg.getY();
        float distance = (float) Math.sqrt(normalX * normalX + normalY * normalY);

        // Avoid division by zero
        if (distance == 0) return;

        float unitNormalX = normalX / distance;
        float unitNormalY = normalY / distance;

        float dotProduct = velocityX * unitNormalX + velocityY * unitNormalY;

        // Don't reflect if the objects are already moving apart
        if (dotProduct >= 0) {
            return;
        }

        // Apply the reflection formula: v' = v - 2 * (v . n) * n
        velocityX = velocityX - 2 * dotProduct * unitNormalX;
        velocityY = velocityY - 2 * dotProduct * unitNormalY;
    }

    public void update(int screenWidth, int screenHeight) {
        if (isMoving) {
            // Apply gravity
            velocityY += gravity;

            // Update position
            x += velocityX;
            y += velocityY;

            // Check for wall collisions
            if (x - radius < 0) {
                x = radius;
                velocityX *= -1;
            } else if (x + radius > screenWidth) {
                x = screenWidth - radius;
                velocityX *= -1;
            }

            if (y - radius < 0) {
                y = radius;
                velocityY *= -1;
            }
        }
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
