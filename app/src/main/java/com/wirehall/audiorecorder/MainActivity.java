package com.wirehall.audiorecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wirehall.audiorecorder.explorer.FileListFragment;
import com.wirehall.audiorecorder.explorer.FileUtils;
import com.wirehall.audiorecorder.visualizer.VisualizerFragment;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements VisualizerFragment.VisualizerMPSession, FileListFragment.FileListFragmentListener {

    public static final int PERMISSION_REQUEST_CODE = 102;
    public static final String[] APP_PERMS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private MediaRecorder mediaRecorder;

    private enum MediaRecorderState {RECORDING, PAUSED, STOPPED}

    private static MediaRecorderState mediaRecorderState = MediaRecorderState.STOPPED;

    ImageButton btnRecordPause, btnDelete, btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ActivityCompat.requestPermissions(this, APP_PERMS, PERMISSION_REQUEST_CODE);

        btnRecordPause = findViewById(R.id.ib_record);
        btnRecordPause.setEnabled(true);
        btnDelete = findViewById(R.id.ib_delete);
        btnDelete.setEnabled(false);
        btnStop = findViewById(R.id.ib_stop);
        btnStop.setEnabled(false);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void recordPauseBtnClicked(View view) {

        switch (mediaRecorderState) {
            case RECORDING:
                mediaRecorder.pause();
                mediaRecorderState = MediaRecorderState.PAUSED;
                btnRecordPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mic_black_24dp));
                btnRecordPause.setEnabled(true);
                btnStop.setEnabled(true);
                break;

            case PAUSED:
                mediaRecorder.resume();
                mediaRecorderState = MediaRecorderState.RECORDING;
                btnRecordPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_black_24dp));
                btnRecordPause.setEnabled(true);
                btnStop.setEnabled(true);
                break;

            case STOPPED:
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

                mediaRecorderState = MediaRecorderState.RECORDING;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    btnRecordPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_black_24dp));
                    btnRecordPause.setEnabled(true);
                } else {
                    btnRecordPause.setEnabled(false);
                }

                //setImageButtonEnabled(true, btnStop, R.drawable.ic_stop_black_24dp);
                btnStop.setEnabled(true);

            default:
                break;
        }
    }

    public void deleteBtnClicked(View view) {
        ImageButton btnDelete = (ImageButton) view;
    }

    public void stopBtnClicked(View view) {
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaRecorder = null;

        btnRecordPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mic_black_24dp));
        btnRecordPause.setEnabled(true);
        //setImageButtonEnabled(false, btnStop, R.drawable.ic_stop_black_24dp);
        btnStop.setEnabled(false);
        mediaRecorderState = MediaRecorderState.STOPPED;

        Toast.makeText(this, "Recording saved successfully.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getAudioSessionIdOfMediaPlayer() {
        return mediaPlayer.getAudioSessionId();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isPermissionAccepted = false;
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                isPermissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (isPermissionAccepted) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.list_fragment_container, FileListFragment.newInstance());
            ft.add(R.id.visualizer_fragment_container, VisualizerFragment.newInstance());
            ft.commit();
        } else {
            finish();
        }
    }

    @Override
    public void onFileItemClicked(String filePath) {
        RecordingUtils.playAudio(filePath, mediaPlayer);
    }


    /**
     * Sets the specified image button to the given state, while modifying or
     * "graying-out" the icon as well
     *
     * @param enabled     The state of the menu imageButton
     * @param imageButton The menu imageButton to modify
     * @param iconResId   The icon ID
     */
    public void setImageButtonEnabled(boolean enabled, ImageButton imageButton, int iconResId) {
        imageButton.setEnabled(enabled);
        Drawable originalIcon = getResources().getDrawable(iconResId);
        Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        imageButton.setImageDrawable(icon);
    }

    /**
     * Mutates and applies a filter that converts the given drawable to a Gray
     * image. This method may be used to simulate the color of disable icons in
     * Honeycomb's ActionBar.
     *
     * @return a mutated version of the given drawable with a color filter
     * applied.
     */
    public Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    }

}
