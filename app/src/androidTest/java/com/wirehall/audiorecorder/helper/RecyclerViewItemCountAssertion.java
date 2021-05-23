package com.wirehall.audiorecorder.helper;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

import static org.hamcrest.Matchers.is;

public class RecyclerViewItemCountAssertion implements ViewAssertion {
  private final Matcher<Integer> matcher;

  public static RecyclerViewItemCountAssertion withItemCount(int expectedCount) {
    return withItemCount(is(expectedCount));
  }

  public static RecyclerViewItemCountAssertion withItemCount(Matcher<Integer> matcher) {
    return new RecyclerViewItemCountAssertion(matcher);
  }

  private RecyclerViewItemCountAssertion(Matcher<Integer> matcher) {
    this.matcher = matcher;
  }

  @Override
  public void check(View view, NoMatchingViewException noViewFoundException) {
    if (noViewFoundException != null) {
      throw noViewFoundException;
    }

    RecyclerView recyclerView = (RecyclerView) view;
    Adapter adapter = recyclerView.getAdapter();
    MatcherAssert.assertThat(adapter.getItemCount(), matcher);
  }
}
