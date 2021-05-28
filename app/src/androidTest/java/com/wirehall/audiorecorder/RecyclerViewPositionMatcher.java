package com.wirehall.audiorecorder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class RecyclerViewPositionMatcher {
  public static Matcher<View> atPosition(
      final int position, @NonNull final Matcher<View> itemMatcher) {

    return new BoundedMatcher(RecyclerView.class) {
      @Override
      protected boolean matchesSafely(Object view) {
        RecyclerView.ViewHolder viewHolder =
            ((RecyclerView) view).findViewHolderForAdapterPosition(position);
        if (viewHolder == null) {
          // has no item on such position
          return false;
        }
        return itemMatcher.matches(viewHolder.itemView);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("has item at position " + position + ": ");
        itemMatcher.describeTo(description);
      }
    };
  }
}
