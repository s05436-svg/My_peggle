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
    private float rotation = 0f;

    public Cannon(Context context, float x, float y, float width, float height) {
        super(x, y);
        this.width = width;
        this.height = height;
        setMovable(false);

        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cannon);
        this.bitmap = Bitmap.createScaledBitmap(originalBitmap, (int)width, (int)height, true);
    }

    public void aim(float targetX, float targetY) {
        float dx = targetX - x;
        float dy = targetY - y;
        // Add 90 degrees because our cannon bitmap points upwards
        this.rotation = (float) Math.toDegrees(Math.atan2(dy, dx)) - 90;
    }

    public Ball fire(Context context) {
        // Convert rotation back to radians for trigonometry, adjusting for the initial +90 offset
        float angleInRadians = (float) Math.toRadians(rotation - 90);
        float nozzleDistance = height / 2; // Fire from the tip of the cannon
        float fireX = x + nozzleDistance * (float) Math.cos(angleInRadians);
        float fireY = y + nozzleDistance * (float) Math.sin(angleInRadians);

        return new Ball(context, fireX, fireY, 15);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.rotate(rotation, x, y);
        canvas.drawBitmap(bitmap, x - width / 2, y - height / 2, null);
        canvas.restore();
    }

    @Override
    public boolean isTouched(float touchX, float touchY) {
        return false;
    }
}
