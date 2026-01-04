package com.My_peggle.shapes;

import android.graphics.Canvas;

public abstract class BaseShape {
    protected float x, y;
    protected boolean isMovable = true;

    public BaseShape(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setPosition(float x, float y) {
        if (isMovable) {
            this.x = x;
            this.y = y;
        }
    }

    public abstract void draw(Canvas canvas);

    public abstract boolean isTouched(float touchX, float touchY);

    public void setMovable(boolean movable) {
        this.isMovable = movable;
    }

    public boolean isMovable() {
        return isMovable;
    }
}
