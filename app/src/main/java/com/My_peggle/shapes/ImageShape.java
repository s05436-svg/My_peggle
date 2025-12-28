package com.My_peggle.shapes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class ImageShape extends BaseShape {

    private Bitmap bitmap;
    private float width;
    private float height;

    public ImageShape(Context context, float x, float y, float width, float height, int resId) {
        this(context, x, y, width, height, resId, true);
    }

    public ImageShape(Context context, float x, float y, float width, float height, int resId, boolean movable) {
        super(x, y, android.graphics.Color.WHITE);
        setMovable(movable);

        this.width = width;
        this.height = height;

        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        this.bitmap = Bitmap.createScaledBitmap(originalBitmap, (int)width, (int)height, true);
    }

    @Override
    public void draw(Canvas canvas) {
        float drawLeft = x - (width / 2);
        float drawTop = y - (height / 2);

        canvas.drawBitmap(bitmap, drawLeft, drawTop, paint);
    }

    @Override
    public boolean isTouched(float touchX, float touchY) {
        float left = x - (width / 2);
        float right = x + (width / 2);
        float top = y - (height / 2);
        float bottom = y + (height / 2);

        return touchX >= left && touchX <= right && touchY >= top && touchY <= bottom;
    }
}
