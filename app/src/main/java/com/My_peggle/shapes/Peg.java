package com.My_peggle.shapes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.My_peggle.R;

public class Peg extends BaseShape {

    public enum PegType {
        ORANGE,
        BLUE
    }

    private boolean isHit;
    private float radius;
    private Bitmap bitmap;
    private final PegType type;

    public Peg(Context context, float x, float y, float radius, PegType type) {
        super(x, y);
        this.radius = radius;
        this.type = type;
        this.isHit = false;
        setMovable(false);

        int resId;
        switch (type) {
            case ORANGE:
                resId = R.drawable.orange_ball;
                break;
            case BLUE:
            default:
                resId = R.drawable.blue_ball;
                break;
        }

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

    public PegType getType() {
        return type;
    }

    public boolean isHit() {
        return isHit;
    }

    public void setHit(boolean hit) {
        isHit = hit;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!isHit && bitmap != null) {
            canvas.drawBitmap(bitmap, x - radius, y - radius, null);
        }
    }

    @Override
    public boolean isTouched(float touchX, float touchY) {
        float distance = (float) Math.sqrt(Math.pow(touchX - x, 2) + Math.pow(touchY - y, 2));
        return distance <= radius;
    }
}
