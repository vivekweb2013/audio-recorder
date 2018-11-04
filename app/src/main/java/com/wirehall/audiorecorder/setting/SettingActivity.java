package com.wirehall.audiorecorder.setting;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {
    public static final String KEY_PREF_CONFIRM_DELETE = "confirm_delete";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingFragment())
                .commit();
    }
}
