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

import static com.wirehall.audiorecorder.BuildConfig.BUG_REPORT_URL;
import static com.wirehall.audiorecorder.BuildConfig.PLAY_STORE_APP_URL;
import static com.wirehall.audiorecorder.BuildConfig.PLAY_STORE_WEB_URL;
import static com.wirehall.audiorecorder.BuildConfig.PRIVACY_POLICY_URL;
import static com.wirehall.audiorecorder.BuildConfig.SOURCE_CODE_URL;
import static com.wirehall.audiorecorder.BuildConfig.TWITTER_ACCOUNT_APP_URL;
import static com.wirehall.audiorecorder.BuildConfig.TWITTER_ACCOUNT_WEB_URL;

public class HelperUtils {

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
    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)));
  }

  public static void openSourceCodeIntent(Context context) {
    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_CODE_URL)));
  }

  public static void openBugReportIntent(Context context) {
    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BUG_REPORT_URL)));
  }

  public static void openRateIntent(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = prefs.edit();

    try {
      context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_APP_URL)));
    } catch (ActivityNotFoundException e) {
      context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_WEB_URL)));
    }

    if (editor != null) {
      editor.putBoolean(AppRater.KEY_PREF_RATE_DIALOG_DO_NOT_SHOW, true);
      editor.apply();
    }
  }

  public static void openTwitterIntent(Context context) {
    try {
      context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TWITTER_ACCOUNT_APP_URL)));
    } catch (Exception e) {
      context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TWITTER_ACCOUNT_WEB_URL)));
    }
  }
}
