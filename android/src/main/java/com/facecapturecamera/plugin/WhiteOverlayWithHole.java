package com.facecapturecamera.plugin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

public class WhiteOverlayWithHole extends View {

    private Paint overlayPaint;
    private Path holePath;

    public WhiteOverlayWithHole(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        overlayPaint = new Paint();
        overlayPaint.setColor(0xFFFFFFFF); // white
        overlayPaint.setStyle(Paint.Style.FILL);

        holePath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        holePath.reset();

        float centerX = width / 2f;
        float centerY = height / 2f + 100;
        float radius = Math.min(width, height) / 2.4f;

        holePath.addCircle(centerX, centerY, radius, Path.Direction.CCW);
        holePath.setFillType(Path.FillType.INVERSE_WINDING);

        canvas.save();
        canvas.clipPath(holePath);
        canvas.drawPaint(overlayPaint);
        canvas.restore();
    }
}
