package com.wirehall.audiorecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wirehall.explorer.FileListFragment;
import com.wirehall.explorer.FileUtils;
import com.wirehall.visualizer.VisualizerFragment;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements VisualizerFragment.VisualizerMPSession, FileListFragment.FileListFragmentListener {

    public static final int PERMISSION_REQUEST_CODE = 102;
    public static final String[] APP_PERMS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private MediaPlayer mediaPlayer = new MediaPlayer();;
    private MediaRecorder mediaRecorder;

    private enum MediaRecorderState {STARTED, PAUSED, STOPPED}

    private static MediaRecorderState mediaRecorderState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, APP_PERMS, PERMISSION_REQUEST_CODE);
        setContentView(R.layout.main_activity);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void recordPauseBtnClicked(View view) {
        ImageButton btnRecordPause = (ImageButton) view;
        if (mediaRecorder != null && mediaRecorderState.equals(MediaRecorderState.PAUSED)) {
            mediaRecorder.pause();
            btnRecordPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mic_black_24dp));
        } else {

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            String fullFilePath = FileListFragment.STORAGE_PATH + '/' + FileUtils.generateFileName();
            Log.d("filename", fullFilePath);

            mediaRecorder.setOutputFile(fullFilePath);
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

            btnRecordPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_black_24dp));
        }
    }

    public void deleteBtnClicked(View view) {
        ImageButton btnDelete = (ImageButton) view;
    }

    public void stopBtnClicked(View view) {
        ImageButton btnStop = (ImageButton) view;

        try {
            mediaRecorder.stop();
            mediaRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaRecorder = null;
        Toast.makeText(this, "Recording saved successfully.", Toast.LENGTH_SHORT).show();

        // TODO: Enable the record button, Disable the Stop button
    }

    @Override
    public int getAudioSessionIdOfMediaPlayer() {
        return mediaPlayer.getAudioSessionId();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionToRecordAccepted = false;
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();
    }

    @Override
    public void onFileItemClicked(String filePath) {
        RecordingUtils.playAudio(filePath, mediaPlayer);
    }


}
