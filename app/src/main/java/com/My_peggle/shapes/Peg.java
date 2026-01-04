package com.My_peggle.shapes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class Peg extends BaseShape {
    private boolean isHit;
    private float radius;
    private Bitmap bitmap;

    public Peg(Context context, float x, float y, float radius, int resId) {
        super(x, y);
        this.radius = radius;
        this.isHit = false;

        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        this.bitmap = Bitmap.createScaledBitmap(originalBitmap, (int)(radius*2), (int)(radius*2), true);
    }

    public boolean isHit() {
        return isHit;
    }

    public void setHit(boolean hit) {
        isHit = hit;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!isHit) {
            canvas.drawBitmap(bitmap, x - radius, y - radius, null);
        }
    }

    @Override
    public boolean isTouched(float touchX, float touchY) {
        float distance = (float) Math.sqrt(Math.pow(touchX - x, 2) + Math.pow(touchY - y, 2));
        return distance <= radius;
    }
}
