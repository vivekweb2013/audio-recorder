package com.wirehall.audiorecorder.visualizer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

public class BarVisualizerView extends BaseVisualizerView {

    private float density = 50;
    private int gap;

    public BarVisualizerView(Context context) {
        super(context);
    }

    public BarVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BarVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(@Nullable AttributeSet attrs) {
        this.density = 50;
        this.gap = 4;
        paint.setStyle(Paint.Style.FILL);
    }

    /**
     * sets the density to the bar visualizer i.e the number of bars to be displayed.
     * density can vary from 10 to 256. by default the value is set to 50.
     *
     * @param density density of the bar visualizer
     */
    public void setDensity(float density) {
        this.density = density > 256 ? 256 : (density < 10) ? 10 : density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bytes != null) {
            float barWidth = getWidth() / density;
            float div = bytes.length / density;
            paint.setStrokeWidth(barWidth - gap);

            for (int i = 0; i < density; i++) {
                int bytePosition = (int) Math.ceil(i * div);
                int top = getHeight() + ((byte) (Math.abs(bytes[bytePosition]) + 128)) * getHeight() / 128;
                float barX = (i * barWidth) + (barWidth / 2);
                canvas.drawLine(barX, getHeight(), barX, top, paint);
            }
            super.onDraw(canvas);
        }
    }
}
