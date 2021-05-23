package com.wirehall.audiorecorder;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.wirehall.audiorecorder.helper.EspressoTestsMatchers.withDrawable;
import static com.wirehall.audiorecorder.helper.RecyclerViewItemCountAssertion.withItemCount;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
  public static final int WAIT_MILLIS = 3000;

  @Rule
  public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

  @Rule
  public GrantPermissionRule permissionRule =
      GrantPermissionRule.grant(RECORD_AUDIO, WRITE_EXTERNAL_STORAGE);

  @Test
  public void testLaunch_main_activity() {
    onView(withId(R.id.recorder_container)).check(matches(isDisplayed()));
    onView(withId(R.id.vis_timer_container)).check(matches(isDisplayed()));
    onView(withId(R.id.sb_mp_seek_bar)).check(matches(isDisplayed()));
    onView(withId(R.id.explorer_parent_container)).check(matches(isDisplayed()));
    validateRecordBtnState(R.drawable.ic_mic, not(isEnabled()), not(isEnabled()));
  }

  @Test
  public void testScenario_record_delete() {
    AtomicInteger count = new AtomicInteger();
    storeInitialRVItemCount(count);

    // Start the recording
    onView(withId(R.id.ib_record)).perform(click());
    validateRecordBtnState(R.drawable.ic_pause_white, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Delete the recording
    onView(withId(R.id.ib_delete)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, not(isEnabled()), not(isEnabled()));
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Validate that no file is created
    validateNoFileCreated(count);
  }

  @Test
  public void testScenario_record_stop() {
    AtomicInteger count = new AtomicInteger();
    storeInitialRVItemCount(count);

    // The record icon should set to mic
    validateRecordBtnState(R.drawable.ic_mic, not(isEnabled()), not(isEnabled()));

    // Start the recording
    onView(withId(R.id.ib_record)).perform(click());
    validateRecordBtnState(R.drawable.ic_pause_white, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Stop the recording
    onView(withId(R.id.ib_stop)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, not(isEnabled()), not(isEnabled()));
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Validate that the file is saved
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .check(withItemCount(greaterThan(count.get())));
  }

  @Test
  public void testScenario_record_pause_stop() {
    AtomicInteger count = new AtomicInteger();
    storeInitialRVItemCount(count);

    // Start the recording
    onView(withId(R.id.ib_record)).perform(click());
    // The record icon should change to pause
    validateRecordBtnState(R.drawable.ic_pause_white, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Pause the recording
    onView(withId(R.id.ib_record)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Stop the recording
    onView(withId(R.id.ib_stop)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, not(isEnabled()), not(isEnabled()));
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Validate that the file is saved
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .check(withItemCount(greaterThan(count.get())));
  }

  @Test
  public void testScenario_record_pause_record_stop() {
    AtomicInteger count = new AtomicInteger();
    storeInitialRVItemCount(count);

    // Start the recording
    onView(withId(R.id.ib_record)).perform(click());
    // The record icon should change to pause
    validateRecordBtnState(R.drawable.ic_pause_white, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Pause the recording
    onView(withId(R.id.ib_record)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Start recording again
    onView(withId(R.id.ib_record)).perform(click());
    validateRecordBtnState(R.drawable.ic_pause_white, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Stop the recording
    onView(withId(R.id.ib_stop)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, not(isEnabled()), not(isEnabled()));
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Validate that the file is saved
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .check(withItemCount(greaterThan(count.get())));
  }

  @Test
  public void testScenario_record_pause_record_delete() {
    AtomicInteger count = new AtomicInteger();
    storeInitialRVItemCount(count);

    // Start the recording
    onView(withId(R.id.ib_record)).perform(click());
    // The record icon should change to pause
    validateRecordBtnState(R.drawable.ic_pause_white, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Pause the recording
    onView(withId(R.id.ib_record)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Start recording again
    onView(withId(R.id.ib_record)).perform(click());
    // The record icon should change to pause
    validateRecordBtnState(R.drawable.ic_pause_white, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Delete the recording
    onView(withId(R.id.ib_delete)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, not(isEnabled()), not(isEnabled()));
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(WAIT_MILLIS));

    // Validate that no file is created
    validateNoFileCreated(count);
  }

  private void validateRecordBtnState(
      int recBtnDrawable, Matcher<View> stopBtnMatcher, Matcher<View> deleteBtnMatcher) {

    onView(withId(R.id.ib_record)).check(matches(withDrawable(recBtnDrawable)));
    onView(withId(R.id.ib_stop)).check(matches(stopBtnMatcher));
    onView(withId(R.id.ib_delete)).check(matches(deleteBtnMatcher));
  }

  private void validateNoFileCreated(AtomicInteger prevCount) {
    // Verify that the previous count matches
    // Or the file list is empty (i.e recycler view does not exist)
    try {
      onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
          .check(withItemCount(prevCount.get()));
    } catch (NoMatchingViewException ex) {
      // This is a valid case
      // When there are no files, there is no recycler view added
    }
  }

  private void storeInitialRVItemCount(AtomicInteger count) {
    rule.getScenario()
        .onActivity(
            activity -> {
              count.set(
                  ((RecyclerView) activity.findViewById(R.id.recycler_view))
                      .getAdapter()
                      .getItemCount());
            });
  }

  /** Perform action of waiting for a specific time. */
  public static ViewAction waitFor(final long millis) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isRoot();
      }

      @Override
      public String getDescription() {
        return "Wait for " + millis + " milliseconds.";
      }

      @Override
      public void perform(UiController uiController, final View view) {
        uiController.loopMainThreadForAtLeast(millis);
      }
    };
  }
}
