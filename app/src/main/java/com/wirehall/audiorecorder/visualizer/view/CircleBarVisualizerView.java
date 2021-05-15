package com.wirehall.audiorecorder.visualizer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class CircleBarVisualizerView extends BaseVisualizerView {
  private float[] points;
  private Paint circlePaint;
  private int radius;

  public CircleBarVisualizerView(Context context) {
    super(context);
  }

  public CircleBarVisualizerView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public CircleBarVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void init(@Nullable AttributeSet attrs) {
    paint.setStyle(Paint.Style.STROKE);
    circlePaint = new Paint();
    radius = -1;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (radius == -1) {
      radius = getHeight() < getWidth() ? getHeight() : getWidth();
      radius = (int) (radius * 0.65 / 2);
      double circumference = 2 * Math.PI * radius;
      paint.setStrokeWidth((float) (circumference / 120));
      circlePaint.setStyle(Paint.Style.STROKE);
      circlePaint.setStrokeWidth(4);
    }
    circlePaint.setColor(color);
    canvas.drawCircle(getWidth() >> 1, getHeight() >> 1, radius, circlePaint);
    if (bytes != null) {
      if (points == null || points.length < bytes.length * 4) {
        points = new float[bytes.length * 4];
      }
      double angle = 0;

      for (int i = 0; i < 120; i++, angle += 3) {
        int x = (int) Math.ceil(i * 8.5);
        int t = ((byte) (-Math.abs(bytes[x]) + 128)) * (getHeight() / 4) / 128;

        points[i * 4] = (float) (getWidth() / 2d + radius * Math.cos(Math.toRadians(angle)));

        points[i * 4 + 1] = (float) (getHeight() / 2d + radius * Math.sin(Math.toRadians(angle)));

        points[i * 4 + 2] =
            (float) (getWidth() / 2d + (radius + t) * Math.cos(Math.toRadians(angle)));

        points[i * 4 + 3] =
            (float) (getHeight() / 2d + (radius + t) * Math.sin(Math.toRadians(angle)));
      }

      canvas.drawLines(points, paint);
    }
    super.onDraw(canvas);
  }
}
