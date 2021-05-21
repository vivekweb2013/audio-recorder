package com.wirehall.audiorecorder;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

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
  }
}
