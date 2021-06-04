package com.wirehall.audiorecorder;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.wirehall.audiorecorder.helper.EspressoTestsMatchers.childAtPosition;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityNavDrawerTest {

  @Rule
  public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

  @Rule
  public GrantPermissionRule permissionRule =
      GrantPermissionRule.grant(RECORD_AUDIO, WRITE_EXTERNAL_STORAGE);

  @Test
  public void testNav_launch_settings() {
    openNavDrawer();
    onView(allOf(withId(R.id.nav_settings), isDisplayed())).perform(click());

    onView(withId(androidx.preference.R.id.recycler_view))
        .check(
            matches(
                hasDescendant(
                    allOf(
                        withText(R.string.pref_confirm_delete_summary),
                        withText(R.string.pref_confirm_delete_summary)))));

    // Back to main activity
    onView(
            allOf(
                withContentDescription("Navigate up"),
                childAtPosition(
                    allOf(
                        withId(R.id.action_bar),
                        childAtPosition(withId(R.id.action_bar_container), 0)),
                    1),
                isDisplayed()))
        .perform(click());
  }

  @Test
  public void testNav_launch_privacy_policy() {
    openNavDrawer();
    onView(withId(R.id.nav_privacy_policy)).check(matches(isDisplayed()));

    // Validate share option
    init();
    // Before triggering the sharing intent chooser, stub it out to avoid leaving system UI open
    // after the test is finished.
    intending(hasAction(equalTo(Intent.ACTION_VIEW)))
        .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    onView(allOf(withId(R.id.nav_privacy_policy), isDisplayed())).perform(click());
    intended(hasAction(Intent.ACTION_VIEW));
    release();
  }

  @Test
  public void testNav_launch_source_code() {
    openNavDrawer();
    onView(withId(R.id.nav_source_code)).check(matches(isDisplayed()));

    // Validate share option
    init();
    // Before triggering the sharing intent chooser, stub it out to avoid leaving system UI open
    // after the test is finished.
    intending(hasAction(equalTo(Intent.ACTION_VIEW)))
        .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    onView(allOf(withId(R.id.nav_source_code), isDisplayed())).perform(click());
    intended(hasAction(Intent.ACTION_VIEW));
    release();
  }

  @Test
  public void testNav_launch_bug_report() {
    openNavDrawer();
    onView(withId(R.id.nav_bug_report)).check(matches(isDisplayed()));

    // Validate share option
    init();
    // Before triggering the sharing intent chooser, stub it out to avoid leaving system UI open
    // after the test is finished.
    intending(hasAction(equalTo(Intent.ACTION_VIEW)))
        .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    onView(allOf(withId(R.id.nav_bug_report), isDisplayed())).perform(click());
    intended(hasAction(Intent.ACTION_VIEW));
    release();
  }

  @Test
  public void testNav_launch_rate() {
    openNavDrawer();
    onView(withId(R.id.nav_rate)).check(matches(isDisplayed()));

    // Validate share option
    init();
    // Before triggering the sharing intent chooser, stub it out to avoid leaving system UI open
    // after the test is finished.
    intending(hasAction(equalTo(Intent.ACTION_VIEW)))
        .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    onView(allOf(withId(R.id.nav_rate), isDisplayed())).perform(click());
    intended(hasAction(Intent.ACTION_VIEW));
    release();
  }

  @Test
  public void testNav_launch_twitter() {
    openNavDrawer();
    onView(withId(R.id.nav_twitter)).check(matches(isDisplayed()));

    // Validate share option
    init();
    // Before triggering the sharing intent chooser, stub it out to avoid leaving system UI open
    // after the test is finished.
    intending(hasAction(equalTo(Intent.ACTION_VIEW)))
        .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    onView(allOf(withId(R.id.nav_twitter), isDisplayed())).perform(click());
    intended(hasAction(Intent.ACTION_VIEW));
    release();
  }

  @Test
  public void testLaunch_about_dialog() {
    openNavDrawer();
    onView(withId(R.id.nav_about)).check(matches(isDisplayed()));

    onView(allOf(withId(R.id.nav_about), isDisplayed())).perform(click());
    onView(withId(R.id.tv_privacy_policy_link)).check(matches(isDisplayed()));
    onView(withId(R.id.btn_about_dialog_close)).perform(click());
  }

  private void openNavDrawer() {
    onView(
            allOf(
                withContentDescription("Open Navigation Drawer"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(is("com.google.android.material.appbar.AppBarLayout")),
                            0)),
                    1),
                isDisplayed()))
        .perform(click());
  }
}
