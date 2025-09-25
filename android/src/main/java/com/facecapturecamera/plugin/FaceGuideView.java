package com.facecapturecamera.plugin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class FaceGuideView extends View {

    private Paint paint;
    private int borderColor = Color.RED;

    // Store last drawn circle's bounds
    private RectF circleBounds;

    public FaceGuideView(Context context) {
        super(context);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6); // Border thickness
        paint.setAntiAlias(true);
        paint.setColor(borderColor);
    }

    public void setBorderColor(int color) {
        borderColor = color;
        paint.setColor(borderColor);
        invalidate(); // Redraw the view with new color
    }

    public RectF getCircleBounds() {
        return circleBounds;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2 + 100; // keep this as per your design
        float radius = Math.min(getWidth(), getHeight()) / 2.4f;

        circleBounds = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        canvas.drawCircle(centerX, centerY, radius, paint);
    }
}
