package com.wirehall.audiorecorder.visualizer.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/** Base class that contains common implementation for all visualizer views. */
public abstract class BaseVisualizerView extends View {
  protected final Paint paint = new Paint();
  protected byte[] bytes;
  protected int color = Color.BLUE;

  protected BaseVisualizerView(Context context) {
    super(context);
    setContentDescription(null);
    init(null);
  }

  protected BaseVisualizerView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setContentDescription(null);
    init(attrs);
  }

  protected BaseVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setContentDescription(null);
    init(attrs);
  }

  public static float computeStroke(int strokeWidth) {
    if ((strokeWidth < 1)) {
      return 0.005f;
    } else if (strokeWidth > 10) return 0.05f;
    else return strokeWidth * 0.005f;
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

  /** @param bytes The view is drawn based on this input */
  public void setBytes(byte[] bytes) {
    BaseVisualizerView.this.bytes = bytes;
  }

  protected abstract void init(@Nullable AttributeSet attributeSet);
}
