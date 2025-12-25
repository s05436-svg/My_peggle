package com.My_peggle.shapes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class ImageShape extends BaseShape {

    private Bitmap bitmap;
    private float width;
    private float height;

    // בנאי (Constructor) שמקבל את המיקום, הגודל הרצוי, והמזהה של התמונה
    public ImageShape(Context context, float x, float y, float width, float height, int resId) {
        // מעבירים צבע 0 או כל דבר אחר ל-super, כי בתמונה הצבע של ה-Paint פחות קריטי
        super(x, y, android.graphics.Color.WHITE);

        this.width = width;
        this.height = height;

        // 1. טעינת התמונה המקורית מהמשאבים
        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), resId);

        // 2. שינוי גודל התמונה לגודל שביקשת (width/height)
        // זה קריטי כדי שהתמונה לא תהיה ענקית מדי על המסך
        this.bitmap = Bitmap.createScaledBitmap(originalBitmap, (int)width, (int)height, true);
    }

    @Override
    public void draw(Canvas canvas) {
        // חישוב המיקום כך ש-(x,y) יהיה המרכז של התמונה (כמו בעיגול)
        // אם לא נעשה את החיסור הזה, התמונה תצויר כשהנקודה (x,y) היא הפינה השמאלית-עליונה שלה
        float drawLeft = x - (width / 2);
        float drawTop = y - (height / 2);

        canvas.drawBitmap(bitmap, drawLeft, drawTop, paint);
    }

    @Override
    public boolean isTouched(float touchX, float touchY) {
        // בדיקה האם הלחיצה היא בתוך המלבן של התמונה
        float left = x - (width / 2);
        float right = x + (width / 2);
        float top = y - (height / 2);
        float bottom = y + (height / 2);

        return touchX >= left && touchX <= right && touchY >= top && touchY <= bottom;
    }
}