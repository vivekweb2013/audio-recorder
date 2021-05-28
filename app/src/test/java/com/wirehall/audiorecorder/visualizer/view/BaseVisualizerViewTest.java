package com.wirehall.audiorecorder.visualizer.view;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class BaseVisualizerViewTest {
  private static final double DELTA = 1e-15;

  @Test
  public void test_computeStroke_with_input_range() {
    float stroke = BaseVisualizerView.computeStroke(-1);
    assertThat(0.005f, equalTo(stroke));

    float stroke2 = BaseVisualizerView.computeStroke(11);
    assertThat(0.05f, equalTo(stroke2));

    float stroke3 = BaseVisualizerView.computeStroke(5);
    assertEquals(5 * 0.005f, stroke3, 0.001);
  }
}
