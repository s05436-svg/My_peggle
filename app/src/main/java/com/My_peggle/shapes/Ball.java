package com.My_peggle.shapes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.My_peggle.R;

public class Ball extends BaseShape {

    // Enum to define the different types of balls
    public enum BallType {
        ORANGE,
        BLUE,
        SILVER
    }

    private float velocityX;
    private float velocityY;
    private float radius;
    private Bitmap bitmap;
    private final BallType type;

    public Ball(Context context, float x, float y, float radius, BallType type) {
        super(x, y);
        this.radius = radius;
        this.type = type;
        this.velocityX = 0;
        this.velocityY = 0;

        int resId;
        // Set properties based on the ball type
        switch (type) {
            case ORANGE:
                resId = R.drawable.ball_orange; // הערה: עליך ליצור תמונה בשם זה
                setMovable(false); // כדור כתום קבוע במקום
                break;
            case BLUE:
                resId = R.drawable.ball_blue;   // הערה: עליך ליצור תמונה בשם זה
                setMovable(false); // כדור כחול קבוע במקום
                break;
            case SILVER:
            default:
                resId = R.drawable.ball_silver; // הערה: עליך ליצור תמונה בשם זה
                setMovable(true);  // כדור כסוף יכול לזוז
                break;
        }

        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        if (originalBitmap != null) {
            this.bitmap = Bitmap.createScaledBitmap(originalBitmap, (int) (radius * 2), (int) (radius * 2), true);
        }
    }

    public BallType getType() {
        return type;
    }

    public void setVelocity(float vx, float vy) {
        // Only movable balls can have velocity
        if (isMovable()) {
            this.velocityX = vx;
            this.velocityY = vy;
        }
    }

    public void update() {
        // Only movable balls update their position based on velocity
        if (isMovable()) {
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
