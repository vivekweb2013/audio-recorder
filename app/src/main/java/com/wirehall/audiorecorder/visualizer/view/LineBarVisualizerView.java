package com.wirehall.audiorecorder.visualizer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class LineBarVisualizerView extends BaseVisualizerView {
    private Paint middleLine;
    private float density;
    private int gap;

    public LineBarVisualizerView(Context context) {
        super(context);
    }

    public LineBarVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LineBarVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(@Nullable AttributeSet attrs) {
        density = 50;
        gap = 4;
        middleLine = new Paint();
        middleLine.setColor(Color.BLUE);
    }

    /**
     * sets the density to the Bar visualizer i.e the number of bars to be displayed.
     * density can vary from 10 to 256. by default the value is set to 50.
     *
     * @param density density of the bar visualizer
     */
    public void setDensity(float density) {
        this.density = density > 256 ? 256 : (density < 10) ? 10 : density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (middleLine.getColor() != Color.BLUE) {
            middleLine.setColor(color);
        }
        if (bytes != null) {
            float barWidth = getWidth() / density;
            float div = bytes.length / density;
            canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, middleLine);
            paint.setStrokeWidth(barWidth - gap);

            for (int i = 0; i < density; i++) {
                int bytePosition = (int) Math.ceil(i * div);
                int top = canvas.getHeight() / 2 + (128 - Math.abs(bytes[bytePosition])) * (canvas.getHeight() / 2) / 128;

                int bottom = canvas.getHeight() / 2 - (128 - Math.abs(bytes[bytePosition])) * (canvas.getHeight() / 2) / 128;

                float barX = (i * barWidth) + (barWidth / 2);
                canvas.drawLine(barX, bottom, barX, canvas.getHeight() / 2, paint);
                canvas.drawLine(barX, top, barX, canvas.getHeight() / 2, paint);
            }
            super.onDraw(canvas);
        }
    }
}
