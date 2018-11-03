package com.wirehall.audiorecorder.explorer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.explorer.model.Recording;


public class FileInformationDialog extends Dialog implements android.view.View.OnClickListener {
    private Context context;
    private Recording recording;

    public FileInformationDialog(Context context, Recording recording) {
        super(context);
        this.context = context;
        this.recording = recording;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.file_info_dialog);
        Button closeButton = findViewById(R.id.btn_file_dialog_close);
        String fileInfoString = context.getResources().getString(R.string.tv_file_info, recording.getName(), recording.getSizeInString(), recording.getDurationInString(), recording.getPath());
        TextView fileInfoTextView = findViewById(R.id.tv_file_info);
        fileInfoTextView.setText(fileInfoString);
        closeButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
