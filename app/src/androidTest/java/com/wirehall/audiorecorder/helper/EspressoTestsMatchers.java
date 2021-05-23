package com.wirehall.audiorecorder.helper;

import android.view.View;

import org.hamcrest.Matcher;

public class EspressoTestsMatchers {

  public static Matcher<View> withDrawable(final int resourceId) {
    return new DrawableMatcher(resourceId);
  }

  public static Matcher<View> noDrawable() {
    return new DrawableMatcher(DrawableMatcher.EMPTY);
  }

  public static Matcher<View> hasDrawable() {
    return new DrawableMatcher(DrawableMatcher.ANY);
  }
}
