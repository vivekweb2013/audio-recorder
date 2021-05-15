package com.wirehall.audiorecorder.explorer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.explorer.model.Recording;

public class FileInformationDialog extends Dialog implements View.OnClickListener {
  private final Recording recording;

  public FileInformationDialog(Context context, Recording recording) {
    super(context);
    this.recording = recording;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.file_info_dialog);
    Button closeButton = findViewById(R.id.btn_file_info_dialog_close);
    Spanned fileInfoString =
        HtmlCompat.fromHtml(
            getContext()
                .getResources()
                .getString(
                    R.string.tv_file_info,
                    recording.getName(),
                    recording.getSizeInString(),
                    recording.getDurationDetailedInString(),
                    recording.getPath()),
            HtmlCompat.FROM_HTML_MODE_LEGACY);
    TextView fileInfoTextView = findViewById(R.id.tv_file_info);
    fileInfoTextView.setText(fileInfoString);
    closeButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    dismiss();
  }
}
