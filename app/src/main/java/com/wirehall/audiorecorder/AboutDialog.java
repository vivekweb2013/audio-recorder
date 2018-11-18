package com.wirehall.audiorecorder;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


public class AboutDialog extends Dialog implements View.OnClickListener {
    private static final String PRIVACY_POLICY_URL = "https://sites.google.com/view/audio-recorder-privacy-policy";

    public AboutDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.about_dialog);

        Button closeButton = findViewById(R.id.btn_about_dialog_close);
        closeButton.setOnClickListener(this);

        Button privacyPolicyButton = findViewById(R.id.btn_privacy_policy);
        privacyPolicyButton.setOnClickListener(this);

        String versionName = getContext().getResources().getString(R.string.label_version, BuildConfig.VERSION_NAME);
        TextView versionTextView = findViewById(R.id.tv_about_dialog_version);
        versionTextView.setText(versionName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_privacy_policy:
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)));
                break;
            case R.id.btn_about_dialog_close:
                dismiss();
                break;
            default:
                dismiss();
                break;
        }
    }
}
