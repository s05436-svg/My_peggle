package com.My_peggle.shapes;
public class TeleportCircle extends CircleShape {
    private final int originalColor;
    private final int activeColor;
    public boolean isActive = false;
    public TeleportCircle(float x, float y, float radius, int color, int activeColor) {
        super(x, y, radius, color);
        this.originalColor = color;
        this.activeColor = activeColor;
    }

    public void setActive(boolean active) {
        this.isActive = active;
        this.paint.setColor(active ? activeColor : originalColor);
    }


}
