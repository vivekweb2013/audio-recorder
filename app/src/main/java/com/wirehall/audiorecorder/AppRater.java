package com.wirehall.audiorecorder;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class AppRater {
  public static final String KEY_PREF_RATE_DIALOG_DO_NOT_SHOW =
      "rate_dialog_do_not_show_" + BuildConfig.VERSION_NAME;
  public static final String KEY_PREF_RATE_DIALOG_FIRST_LAUNCH_TIME =
      "rate_dialog_first_launch_time_" + BuildConfig.VERSION_NAME;
  public static final String KEY_PREF_RATE_DIALOG_LAUNCH_COUNT =
      "rate_dialog_launch_count_" + BuildConfig.VERSION_NAME;

  private static final int DAYS_UNTIL_PROMPT = 3; // Min number of days
  private static final int LAUNCHES_UNTIL_PROMPT = 5; // Min number of launches

  private AppRater() {
    throw new IllegalStateException("Utility class");
  }

  public static void launchIfRequired(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    if (prefs.getBoolean(KEY_PREF_RATE_DIALOG_DO_NOT_SHOW, false)) {
      return;
    }

    SharedPreferences.Editor editor = prefs.edit();

    // Increment launch counter
    long launchCount = prefs.getLong(KEY_PREF_RATE_DIALOG_LAUNCH_COUNT, 0) + 1;
    editor.putLong(KEY_PREF_RATE_DIALOG_LAUNCH_COUNT, launchCount);
    editor.apply();

    // Get date of first launch
    long firstLaunchTime = prefs.getLong(KEY_PREF_RATE_DIALOG_FIRST_LAUNCH_TIME, 0);
    if (firstLaunchTime == 0) {
      firstLaunchTime = System.currentTimeMillis();
      editor.putLong(KEY_PREF_RATE_DIALOG_FIRST_LAUNCH_TIME, firstLaunchTime);
      editor.apply();
    }

    // Wait at least n days before opening
    if (launchCount >= LAUNCHES_UNTIL_PROMPT
        && (System.currentTimeMillis()
            >= firstLaunchTime + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000))) {
      showRateDialog(context);
    }
  }

  private static void showRateDialog(final Context context) {
    final RateDialog rateDialog = new RateDialog(context);
    rateDialog.show();
  }
}
