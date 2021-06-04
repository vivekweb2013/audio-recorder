package com.wirehall.audiorecorder;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.wirehall.audiorecorder.setting.SettingActivity;

public class HelperUtils {
  private static final String PRIVACY_POLICY_URL =
      "https://sites.google.com/view/audio-recorder-privacy-policy";
  private static final String SOURCE_CODE_URL = "https://github.com/vivekweb2013/audio-recorder";
  private static final String BUG_REPORT_URL =
      "https://github.com/vivekweb2013/audio-recorder/issues";

  private static final String APP_PACKAGE_NAME = "com.wirehall.audiorecorder";
  private static final String PLAY_STORE_URL1 = "market://details?id=" + APP_PACKAGE_NAME;
  private static final String PLAY_STORE_URL2 =
      "https://play.google.com/store/apps/details?id=" + APP_PACKAGE_NAME;

  private static final String USERNAME = "vivekweb2013";
  private static final String TWITTER_ACCOUNT_URL1 = "twitter://user?screen_name=" + USERNAME;
  private static final String TWITTER_ACCOUNT_URL2 = "https://twitter.com/" + USERNAME;

  private HelperUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static boolean hasPermissions(Context context, String... permissions) {
    if (context != null && permissions != null) {
      for (String permission : permissions) {
        if (ContextCompat.checkSelfPermission(context, permission)
            != PackageManager.PERMISSION_GRANTED) {
          return false;
        }
      }
    }
    return true;
  }

  public static void openAboutDialog(Context context) {
    AboutDialog aboutDialog = new AboutDialog(context);
    aboutDialog.show();
  }

  public static void openSettings(Context context) {
    Intent intent = new Intent(context, SettingActivity.class);
    context.startActivity(intent);
  }

  public static void openPrivacyPolicyIntent(Context context) {
    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(HelperUtils.PRIVACY_POLICY_URL)));
  }

  public static void openSourceCodeIntent(Context context) {
    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(HelperUtils.SOURCE_CODE_URL)));
  }

  public static void openBugReportIntent(Context context) {
    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(HelperUtils.BUG_REPORT_URL)));
  }

  public static void openRateIntent(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = prefs.edit();

    try {
      context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(HelperUtils.PLAY_STORE_URL1)));
    } catch (ActivityNotFoundException e) {
      context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(HelperUtils.PLAY_STORE_URL2)));
    }

    if (editor != null) {
      editor.putBoolean(AppRater.KEY_PREF_RATE_DIALOG_DO_NOT_SHOW, true);
      editor.apply();
    }
  }

  public static void openTwitterIntent(Context context) {
    try {
      context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TWITTER_ACCOUNT_URL1)));
    } catch (Exception e) {
      context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TWITTER_ACCOUNT_URL2)));
    }
  }
}
