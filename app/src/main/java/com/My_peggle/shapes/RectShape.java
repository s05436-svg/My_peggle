package com.My_peggle.shapes;
import android.graphics.Canvas;
public class RectShape extends com.My_peggle.shapes.BaseShape {
    private final float width, height;
    public RectShape(float x, float y, float w, float h, int color) {
        super(x, y, color);
        this.width = w;
        this.height = h;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRect(x - width / 2, y - height / 2, x + width / 2, y + height / 2, paint);
    }

    @Override
    public boolean isTouched(float touchX, float touchY) {
        float left = x - width / 2;
        float right = x + width / 2;
        float top = y - height / 2;
        float bottom = y + height / 2;
        return touchX >= left && touchX <= right && touchY >= top && touchY <= bottom;
    }


}
