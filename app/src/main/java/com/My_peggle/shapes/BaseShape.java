package com.My_peggle.shapes;
import android.graphics.Canvas;
import android.graphics.Paint;
public abstract class BaseShape {
    protected float x, y;
    protected Paint paint;
    public BaseShape(float x, float y, int color) {
        this.x = x;
        this.y = y;
        this.paint = new Paint();
        this.paint.setColor(color);
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setAntiAlias(true);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public abstract void draw(Canvas canvas);
    public abstract boolean isTouched(float touchX, float touchY);


}
