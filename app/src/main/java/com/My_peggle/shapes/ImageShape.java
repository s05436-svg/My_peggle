package com.My_peggle.shapes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import java.util.ArrayList;
import java.util.List;

public class ImageShape extends BaseShape {

    private List<Bitmap> bitmaps = new ArrayList<>();
    private float width;
    private float height;
    private int currentFrame = 0;

    public ImageShape(Context context, float x, float y, float width, float height, boolean movable, int... resIds) {
        super(x, y);
        setMovable(movable);

        this.width = width;
        this.height = height;

        if (resIds == null || resIds.length == 0) {
            return;
        }

        for (int resId : resIds) {
            Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
            if (originalBitmap != null) {
                bitmaps.add(Bitmap.createScaledBitmap(originalBitmap, (int)width, (int)height, true));
            }
        }
    }

    // Convenience constructor
    public ImageShape(Context context, float x, float y, float width, float height, int... resIds) {
        this(context, x, y, width, height, true, resIds);
    }

    /**
     * Manually sets the visible frame of the image.
     * @param frameIndex The index of the bitmap to display.
     */
    public void setFrame(int frameIndex) {
        if (frameIndex >= 0 && frameIndex < bitmaps.size()) {
            this.currentFrame = frameIndex;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (bitmaps.isEmpty()) {
            return;
        }
        float drawLeft = x - (width / 2);
        float drawTop = y - (height / 2);

        // Draw the currently selected frame
        canvas.drawBitmap(bitmaps.get(currentFrame), drawLeft, drawTop, null);
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
