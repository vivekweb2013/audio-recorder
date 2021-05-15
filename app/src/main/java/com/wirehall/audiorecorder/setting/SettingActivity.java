package com.wirehall.audiorecorder.setting;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {
  public static final String KEY_PREF_CONFIRM_DELETE = "confirm_delete";
  public static final String KEY_PREF_REQUEST_FILENAME = "request_filename";
  public static final String KEY_PREF_LIST_AUDIO_QUALITY = "list_audio_quality";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, new SettingFragment())
        .commit();
  }
}
