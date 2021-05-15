package com.wirehall.audiorecorder.visualizer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class BlazingColorVisualizerView extends BaseVisualizerView {
  private Shader shader;

  public BlazingColorVisualizerView(Context context) {
    super(context);
  }

  public BlazingColorVisualizerView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public BlazingColorVisualizerView(
      Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void init(@Nullable AttributeSet attrs) {}

  @Override
  protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
    super.onSizeChanged(xNew, yNew, xOld, yOld);

    shader =
        new LinearGradient(
            0, 0, 0, yNew, Color.BLUE, Color.WHITE, Shader.TileMode.MIRROR /*or REPEAT*/);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (bytes != null) {
      paint.setShader(shader);
      float lineXWidth = (float) getWidth() / bytes.length;
      for (int i = 0, k = 0; i < (bytes.length - 1) && k < (bytes.length - 1); i++, k++) {
        int top = getHeight() + ((byte) (Math.abs(bytes[k]) + 128)) * getHeight() / 128;
        canvas.drawLine(i * lineXWidth, getHeight(), i * lineXWidth + lineXWidth, top, paint);
      }
      super.onDraw(canvas);
    }
  }
}
