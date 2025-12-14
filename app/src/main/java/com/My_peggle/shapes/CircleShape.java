package com.My_peggle.shapes;
import android.graphics.Canvas;
public class CircleShape extends BaseShape {
    protected float radius;
    public CircleShape(float x, float y, float radius, int color) {
        super(x, y, color);
        this.radius = radius;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, radius, paint);
    }

    @Override
    public boolean isTouched(float touchX, float touchY) {
        double dx = touchX - x;
        double dy = touchY - y;
        return (dx * dx + dy * dy) <= (radius * radius);
    }


}
