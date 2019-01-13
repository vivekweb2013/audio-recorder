package com.wirehall.audiorecorder.visualizer.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Base class that contains common implementation for all visualizer views.
 */

abstract public class BaseVisualizerView extends View {
    protected final Paint paint = new Paint();
    protected byte[] bytes;
    protected int color = Color.BLUE;

    public BaseVisualizerView(Context context) {
        super(context);
        init(null);
    }

    public BaseVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BaseVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * Set the color for visualizer view
     *
     * @param color resource id of color.
     */
    public void setColor(int color) {
        this.color = color;
        this.paint.setColor(this.color);
    }

    /**
     * @param bytes The view is drawn based on this input
     */
    public void setBytes(byte[] bytes) {
        BaseVisualizerView.this.bytes = bytes;
    }

    protected abstract void init(@Nullable AttributeSet attributeSet);
}