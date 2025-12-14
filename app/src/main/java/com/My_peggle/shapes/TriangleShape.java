package com.My_peggle.shapes;
import android.graphics.Canvas;
import android.graphics.Path;
public class TriangleShape extends BaseShape {
    private final float size;
    private final Path path;
    public TriangleShape(float x, float y, float size, int color) {
        super(x, y, color);
        this.size = size;
        this.path = new Path();
    }

    @Override
    public void draw(Canvas canvas) {
        path.reset();
        path.moveTo(x, y - size);           // Top
        path.lineTo(x - size, y + size);    // Bottom Left
        path.lineTo(x + size, y + size);    // Bottom Right
        path.lineTo(x, y - size);           // Close
        path.close();
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean isTouched(float touchX, float touchY) {
        return touchX >= x - size && touchX <= x + size &&
                touchY >= y - size && touchY <= y + size;
    }


}
