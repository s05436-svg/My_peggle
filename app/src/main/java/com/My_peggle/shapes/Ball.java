package com.My_peggle.shapes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.My_peggle.R;

public class Ball extends BaseShape {

    private float velocityX;
    private float velocityY;
    private float radius;
    private Bitmap bitmap;
    private final float speed = 37.0f; // Constant speed for the ball
    private boolean isMoving = false;
    private final float airFriction = 0.995f; // A small amount of negative acceleration
    // Gravity is calculated to counteract friction when the ball moves straight down at its initial speed.
    // gravity = speed * (1 - airFriction)
    private final float gravity = speed * (1-airFriction); // 15.0f * (1 - 0.998f) = 15.0f * 0.002f = 0.03f

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

    public void update() {
        if (isMoving) {
            // Apply air friction
            velocityX *= airFriction;
            velocityY *= airFriction;

            // Apply gravity
            velocityY += gravity;

            // Update position
            x += velocityX;
            y += velocityY;
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
