package com.My_peggle.shapes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.My_peggle.R;

public class Cannon extends BaseShape {
    private Bitmap bitmap;
    private float width;
    private float height;

    public Cannon(Context context, float x, float y, float width, float height) {
        super(x, y);
        this.width = width;
        this.height = height;

        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cannon);
        this.bitmap = Bitmap.createScaledBitmap(originalBitmap, (int)width, (int)height, true);
    }

    public void aim(float targetX, float targetY) {
        // aiming logic would go here
    }

    public Ball fire(Context context) {
        // The ball should be fired from the cannon's "nozzle"
        // For now, let's fire it from the cannon's center.
        return new Ball(context, x, y, 20);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x - width / 2, y - height / 2, null);
    }

    @Override
    public boolean isTouched(float touchX, float touchY) {
        // Cannon isn't typically 'touched' in the same way as other shapes
        return false;
    }
}
