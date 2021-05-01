package com.wirehall.audiorecorder.visualizer.view;

import android.content.Context;
import android.graphics.Canvas;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

public class CircleVisualizerView extends BaseVisualizerView {
    private float[] points;
    private float radiusMultiplier;
    private float strokeWidth = 0.005f;

    public CircleVisualizerView(Context context) {
        super(context);
    }

    public CircleVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(@Nullable AttributeSet attrs) {
    }

    /**
     * set stroke width for your visualizer takes input between 1-10
     *
     * @param strokeWidth stroke width between 1-10
     */
    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = (strokeWidth < 1) ? 0.005f : (strokeWidth > 10) ? 10 * 0.005f : strokeWidth * 0.005f;
    }

    /**
     * set the multiplier to the circle, by default the multiplier is set to 1.
     * you can provide value more than 1 to increase size of the circle visualizer.
     *
     * @param radiusMultiplier multiplies to the radius of the circle.
     */
    public void setRadiusMultiplier(float radiusMultiplier) {
        this.radiusMultiplier = radiusMultiplier;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bytes != null) {
            if (this.radiusMultiplier == 0) {
                this.radiusMultiplier = getHeight() < getWidth() ? getHeight() : getWidth();
                this.radiusMultiplier = (this.radiusMultiplier * 0.65f / 200);
            }
            paint.setStrokeWidth(getHeight() * strokeWidth);
            if (points == null || points.length < bytes.length * 4) {
                points = new float[bytes.length * 4];
            }
            double angle = 0;

            for (int i = 0; i < 360; i++, angle++) {
                points[i * 4] = (float) (getWidth() / 2 + Math.abs(bytes[i * 2]) * radiusMultiplier * Math.cos(Math.toRadians(angle)));
                points[i * 4 + 1] = (float) (getHeight() / 2 + Math.abs(bytes[i * 2]) * radiusMultiplier * Math.sin(Math.toRadians(angle)));
                points[i * 4 + 2] = (float) (getWidth() / 2 + Math.abs(bytes[i * 2 + 1]) * radiusMultiplier * Math.cos(Math.toRadians(angle + 1)));
                points[i * 4 + 3] = (float) (getHeight() / 2 + Math.abs(bytes[i * 2 + 1]) * radiusMultiplier * Math.sin(Math.toRadians(angle + 1)));
            }
            canvas.drawLines(points, paint);
        }
        super.onDraw(canvas);
    }
}
