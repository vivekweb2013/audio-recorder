package com.wirehall.audiorecorder;

import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class MainActivityMenuTest {
  @Rule
  public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

  @Rule
  public GrantPermissionRule permissionRule =
      GrantPermissionRule.grant(RECORD_AUDIO, WRITE_EXTERNAL_STORAGE);

  private static final Context context = getInstrumentation().getTargetContext();

  @Test
  public void testLaunch_about_dialog() {
    openActionBarOverflowOrOptionsMenu(context);
    onView(withText(R.string.about)).perform(click());
    onView(withId(R.id.tv_privacy_policy_link)).check(matches(isDisplayed()));
    onView(withId(R.id.btn_about_dialog_close)).perform(click());
  }

  @Test
  public void testLaunch_settings_activity() {
    openActionBarOverflowOrOptionsMenu(context);
    onView(withText(R.string.settings)).check(matches(isDisplayed()));
    onView(withText(R.string.settings)).perform(click());
  }

  @Test
  public void testLaunch_rate_dialog() {
    openActionBarOverflowOrOptionsMenu(context);
    onView(withText(R.string.rate_this_app)).check(matches(isDisplayed()));
    onView(withText(R.string.rate_this_app)).perform(click());
  }
}
