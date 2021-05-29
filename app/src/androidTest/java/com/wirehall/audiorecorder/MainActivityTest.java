package com.wirehall.audiorecorder;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.wirehall.audiorecorder.helper.EspressoTestsMatchers.withDrawable;
import static com.wirehall.audiorecorder.helper.RecyclerViewHelper.atPosition;
import static com.wirehall.audiorecorder.helper.RecyclerViewHelper.clickChildViewWithId;
import static com.wirehall.audiorecorder.helper.RecyclerViewItemCountAssertion.withItemCount;
import static com.wirehall.audiorecorder.helper.Utils.SEC_10;
import static com.wirehall.audiorecorder.helper.Utils.SEC_3;
import static com.wirehall.audiorecorder.helper.Utils.SEC_5;
import static com.wirehall.audiorecorder.helper.Utils.waitFor;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

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
    onView(isRoot()).perform(waitFor(SEC_3));

    // Delete the recording
    onView(withId(R.id.ib_delete)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, not(isEnabled()), not(isEnabled()));
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(SEC_3));

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
    recordForSeconds(SEC_10);

    // Validate that the file is saved
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .check(withItemCount(greaterThan(count.get())));

    // Play the recording
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .perform(actionOnItemAtPosition(0, click()));
    IntStream.range(0, 6)
        .forEach(i -> onView(withId(R.id.visualizer_fragment_container)).perform(click()));

    // Wait for recording to finish playing
    onView(isRoot()).perform(waitFor(SEC_10 + 1000));

    // Validate info operation
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.ib_file_menu)));
    onView(withText(R.string.file_menu_option_info)).inRoot(isPlatformPopup()).perform(click());
    onView(withId(R.id.tv_file_info)).check(matches(isDisplayed()));
    onView(withId(R.id.btn_file_info_dialog_close)).perform(click());

    // Validate share option
    init();
    // Before triggering the sharing intent chooser, stub it out to avoid leaving system UI open
    // after the test is finished.
    intending(hasAction(equalTo(Intent.ACTION_CHOOSER)))
        .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.ib_file_menu)));
    onView(withText(R.string.file_menu_option_share)).inRoot(isPlatformPopup()).perform(click());
    intended(hasAction(Intent.ACTION_CHOOSER));
    release();

    // Validate rename operation
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.ib_file_menu)));
    onView(withText(R.string.file_menu_option_rename)).inRoot(isPlatformPopup()).perform(click());
    onView(withId(R.id.et_filename_input_dialog)).check(matches(isDisplayed()));
    onView(withId(R.id.btn_filename_input_dialog_cancel)).perform(click());

    // Delete the recording
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.ib_file_menu)));
    onView(withText(R.string.file_menu_option_delete)).inRoot(isPlatformPopup()).perform(click());
    onView(withId(android.R.id.button1)).perform(click());

    // Since we deleted the file, validate that the count is same
    validateNoFileCreated(count);
  }

  @Test
  public void testScenario_play_while_recording() {
    // Start the recording
    recordForSeconds(SEC_3);

    // Start the recording
    onView(withId(R.id.ib_record)).perform(click());
    validateRecordBtnState(R.drawable.ic_pause_white, isEnabled(), isEnabled());
    // Play the recording, media should not be played since recording is going on
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .perform(actionOnItemAtPosition(0, click()));

    // Ensure that the play button has play-arrow icon on file row
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .check(matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_play_arrow_white)))));
    // Ensure that recording is still going on
    validateRecordBtnState(R.drawable.ic_pause_white, isEnabled(), isEnabled());

    // Pause the recording
    onView(withId(R.id.ib_record)).perform(click());
    // Play the recording, media should not be played since recording is going on
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .perform(actionOnItemAtPosition(0, click()));

    // Ensure that the play button has play-arrow icon on file row
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .check(matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_play_arrow_white)))));
    // Ensure that recording is still going on
    validateRecordBtnState(R.drawable.ic_mic, isEnabled(), isEnabled());

    // Stop the recording
    onView(withId(R.id.ib_stop)).perform(click());
  }

  @Test
  public void testScenario_play_while_playing() {
    // Start 1st recording
    recordForSeconds(SEC_5);

    // Start 2nd recording
    recordForSeconds(SEC_10);

    // Play the 1st recording
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .perform(actionOnItemAtPosition(0, click()));

    // Play the 2nd recording before finishing 1st one
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .perform(actionOnItemAtPosition(1, click()));

    // Ensure that the play button has play-arrow icon on 1st row
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .check(matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_play_arrow_white)))));
  }

  @Test
  public void testScenario_record_while_playing() {
    // Start recording
    recordForSeconds(SEC_10);

    // Play the recording
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .perform(actionOnItemAtPosition(0, click()));

    // Start the recording while the media is playing
    onView(withId(R.id.ib_record)).perform(click());
    validateRecordBtnState(R.drawable.ic_pause_white, isEnabled(), isEnabled());

    // Ensure that the play button has play-arrow icon on file row (i.e media is stopped playing)
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .check(matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_play_arrow_white)))));

    // Stop the recording
    onView(withId(R.id.ib_stop)).perform(click());
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
    onView(isRoot()).perform(waitFor(SEC_3));

    // Pause the recording
    onView(withId(R.id.ib_record)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(SEC_3));

    // Stop the recording
    onView(withId(R.id.ib_stop)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, not(isEnabled()), not(isEnabled()));
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(SEC_3));

    // Validate that the file is saved
    onView(allOf(isDisplayed(), withId(R.id.recycler_view)))
        .check(withItemCount(greaterThan(count.get())));
  }

  @Test
  public void testScenario_record_pause_record_stop() {
    // Start the recording
    onView(withId(R.id.ib_record)).perform(click());
    // The record icon should change to pause
    validateRecordBtnState(R.drawable.ic_pause_white, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(SEC_3));

    // Pause the recording
    onView(withId(R.id.ib_record)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, isEnabled(), isEnabled());
    // Start recording again
    recordForSeconds(SEC_3);
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
    onView(isRoot()).perform(waitFor(SEC_3));

    // Pause the recording
    onView(withId(R.id.ib_record)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(SEC_3));

    // Start recording again
    onView(withId(R.id.ib_record)).perform(click());
    // The record icon should change to pause
    validateRecordBtnState(R.drawable.ic_pause_white, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(SEC_3));

    // Delete the recording
    onView(withId(R.id.ib_delete)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, not(isEnabled()), not(isEnabled()));
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(SEC_3));

    // Validate that no file is created
    validateNoFileCreated(count);
  }

  private void recordForSeconds(int secs) {
    onView(withId(R.id.ib_record)).perform(click());
    validateRecordBtnState(R.drawable.ic_pause_white, isEnabled(), isEnabled());
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(secs));

    // Stop the recording
    onView(withId(R.id.ib_stop)).perform(click());
    validateRecordBtnState(R.drawable.ic_mic, not(isEnabled()), not(isEnabled()));
    // Wait for few seconds
    onView(isRoot()).perform(waitFor(SEC_3));
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
}
