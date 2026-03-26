package com.My_peggle.shapes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.My_peggle.R;

public class BottomHole extends BaseShape {
    private Bitmap bitmap;
    private float width;
    private float height;
    private float speed = 8f;
    private int direction = 1;

    public BottomHole(Context context, float x, float y, float width, float height) {
        super(x, y);
        this.width = width;
        this.height = height;
        Bitmap original = BitmapFactory.decodeResource(context.getResources(), R.drawable.bottom_hole);
        if (original != null) {
            this.bitmap = Bitmap.createScaledBitmap(original, (int) width, (int) height, true);
        }
    }

    public void update(float leftBound, float rightBound) {
        x += speed * direction;
        if (x - width / 2 < leftBound) {
            x = leftBound + width / 2;
            direction = 1;
        } else if (x + width / 2 > rightBound) {
            x = rightBound - width / 2;
            direction = -1;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, x - width / 2, y - height / 2, null);
        }
    }

    @Override
    public boolean isTouched(float touchX, float touchY) {
        return touchX >= x - width / 2 && touchX <= x + width / 2 &&
               touchY >= y - height / 2 && touchY <= y + height / 2;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
