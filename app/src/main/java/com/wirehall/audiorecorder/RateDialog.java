package com.wirehall.audiorecorder;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.preference.PreferenceManager;

public class RateDialog extends Dialog implements View.OnClickListener {

  public RateDialog(Context context) {
    super(context);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.rate_dialog);

    Button rateButton = findViewById(R.id.btn_rate_dialog_rate);
    rateButton.setOnClickListener(this);

    Button remindLaterButton = findViewById(R.id.btn_rate_dialog_remind_me_later);
    remindLaterButton.setOnClickListener(this);

    Button noThanksButton = findViewById(R.id.btn_rate_dialog_no_thx);
    noThanksButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    SharedPreferences.Editor editor = prefs.edit();

    int viewId = v.getId();
    if (viewId == R.id.btn_rate_dialog_rate) {
      HelperUtils.openRateIntent(getContext());
    } else if (viewId == R.id.btn_rate_dialog_remind_me_later) {
      // Do nothing
    } else if (viewId == R.id.btn_rate_dialog_no_thx) {
      if (editor != null) {
        editor.putBoolean(AppRater.KEY_PREF_RATE_DIALOG_DO_NOT_SHOW, true);
        editor.apply();
      }
    } else {
      throw new IllegalStateException("Unexpected value: " + viewId);
    }
    dismiss();
  }
}
