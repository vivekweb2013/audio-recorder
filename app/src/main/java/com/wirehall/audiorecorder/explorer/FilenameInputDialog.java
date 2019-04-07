package com.wirehall.audiorecorder.explorer;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.explorer.model.Recording;

import java.io.File;

public class FilenameInputDialog extends Dialog implements android.view.View.OnClickListener {
    private static final String TAG = FilenameInputDialog.class.getName();

    private final String filePath;
    private Recording recording;

    public FilenameInputDialog(Context context, String filePath) {
        super(context);
        this.filePath = filePath;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.filename_input_dialog);
        Button cancelButton = findViewById(R.id.btn_filename_input_dialog_cancel);
        cancelButton.setOnClickListener(this);
        Button okButton = findViewById(R.id.btn_filename_input_dialog_ok);

        final EditText editText = findViewById(R.id.et_filename_input_dialog);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String newRecordingName = editText.getText().toString();
                    if (newRecordingName.trim().isEmpty()) {
                        editText.setHintTextColor(Color.RED);
                        return;
                    }
                    File sourceFile = new File(filePath);
                    File targetFile = new File(sourceFile.getParent(),
                            newRecordingName + FileUtils.DEFAULT_REC_FILENAME_EXTENSION);
                    if (sourceFile.exists() && sourceFile.renameTo(targetFile)) {
                        recording = new Recording();
                        recording.setName(newRecordingName);
                        recording.setPath(targetFile.getPath());
                    } else {
                        Log.e(TAG, "Problem renaming file: " + filePath + " to: " + newRecordingName);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                dismiss();
            }
        });
    }

    public Recording getRenamedRecording() {
        return recording;
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
