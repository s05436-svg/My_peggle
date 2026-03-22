package com.My_peggle.shapes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.My_peggle.R;

public class Peg extends BaseShape {

    public enum PegType {
        ORANGE,
        BLUE
    }

    private boolean isHit;
    private float radius;
    private Bitmap bitmap;
    private Bitmap brightBitmap;
    private final PegType type;
    
    // Animation fields
    private boolean isAnimating = false;
    private float animationProgress = 0f; // 0 to 1
    private Paint animationPaint;

    public Peg(Context context, float x, float y, float radius, PegType type) {
        super(x, y);
        this.radius = radius;
        this.type = type;
        this.isHit = false;
        setMovable(false);
        
        animationPaint = new Paint();
        animationPaint.setAntiAlias(true);

        int resId;
        int brightResId;
        switch (type) {
            case ORANGE:
                resId = R.drawable.orange_ball;
                brightResId = R.drawable.orange_ball_bright;
                break;
            case BLUE:
            default:
                resId = R.drawable.blue_ball;
                brightResId = R.drawable.blue_ball_bright;
                break;
        }

        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        if (originalBitmap != null) {
            this.bitmap = Bitmap.createScaledBitmap(originalBitmap, (int) (radius * 2), (int) (radius * 2), true);
        }

        Bitmap originalBrightBitmap = BitmapFactory.decodeResource(context.getResources(), brightResId);
        if (originalBrightBitmap != null) {
            this.brightBitmap = Bitmap.createScaledBitmap(originalBrightBitmap, (int) (radius * 2), (int) (radius * 2), true);
        }
    }

    public void startAnimation() {
        isAnimating = true;
    }

    public boolean updateAnimation() {
        if (isAnimating) {
            animationProgress += 0.08f; // Speed of animation
            return animationProgress >= 1.0f; // Returns true when done
        }
        return false;
    }

    public void hit() {
        isHit = true;
    }

    public float getRadius() {
        return radius;
    }

    public float getX() { 
        return x;
    }

    public float getY() { 
        return y;
    }

    public PegType getType() {
        return type;
    }

    public boolean isHit() {
        return isHit;
    }

    @Override
    public void draw(Canvas canvas) {
        Bitmap currentBitmap = isHit ? brightBitmap : bitmap;
        if (currentBitmap != null) {
            if (isAnimating) {
                // Expand effect: scale from 1.0 to 1.5
                float scale = 1.0f + (animationProgress * 0.5f);
                // Fade effect: alpha from 255 to 0
                int alpha = (int) (255 * (1.0f - animationProgress));
                animationPaint.setAlpha(alpha);
                
                float drawRadius = radius * scale;
                canvas.save();
                // We draw using the alpha paint
                canvas.drawBitmap(Bitmap.createScaledBitmap(currentBitmap, (int)(drawRadius*2), (int)(drawRadius*2), true), 
                                x - drawRadius, y - drawRadius, animationPaint);
                canvas.restore();
            } else {
                canvas.drawBitmap(currentBitmap, x - radius, y - radius, null);
            }
        }
    }

    @Override
    public boolean isTouched(float touchX, float touchY) {
        float distance = (float) Math.sqrt(Math.pow(touchX - x, 2) + Math.pow(touchY - y, 2));
        return distance <= radius;
    }
}
