package com.wirehall.audiorecorder.helper;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class RecyclerViewHelper {
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

  public static ViewAction clickChildViewWithId(final int id) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return null;
      }

      @Override
      public String getDescription() {
        return "Click on a child view with specified id.";
      }

      @Override
      public void perform(UiController uiController, View view) {
        View v = view.findViewById(id);
        v.performClick();
      }
    };
  }
}
