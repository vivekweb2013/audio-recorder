package com.wirehall.audiorecorder;

import android.annotation.TargetApi;
import android.support.annotation.RequiresApi;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wirehall.audiorecorder.explorer.FileListFragment;
import com.wirehall.audiorecorder.explorer.FileUtils;

import java.io.IOException;

/**
 * This is a singleton class for controlling the recording operations
 */
public class RecordingController {
    private static final String TAG = RecordingController.class.getName();

    private static RecordingController recordingController;

    private AppCompatActivity activity;
    private MediaRecorder mediaRecorder;
    private MediaRecorderState mediaRecorderState = MediaRecorderState.STOPPED;
    private ImageButton btnRecordPause, btnDelete, btnStop;

    private RecordingController() {
        // Private Constructor
    }

    public static RecordingController getInstance() {
        if (recordingController == null) {
            recordingController = new RecordingController();
        }
        return recordingController;
    }

    public void init(AppCompatActivity activity) {
        this.activity = activity;
        btnRecordPause = activity.findViewById(R.id.ib_record);
        btnDelete = activity.findViewById(R.id.ib_delete);
        btnStop = activity.findViewById(R.id.ib_stop);

        if (btnRecordPause != null && btnDelete != null && btnStop != null) {
            btnRecordPause.setEnabled(true);
            btnDelete.setEnabled(false);
            btnStop.setEnabled(false);
        } else {
            Log.e(TAG, "some of the resources are not found! btnRecordPause:" + btnRecordPause + " btnDelete:" + btnDelete + " btnStop:" + btnStop);
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startPauseRecording() {
        switch (mediaRecorderState) {
            case RECORDING:
                mediaRecorder.pause();
                mediaRecorderState = MediaRecorderState.PAUSED;
                btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_mic_black_24dp));
                btnRecordPause.setEnabled(true);
                btnStop.setEnabled(true);
                Toast.makeText(activity, "Recording Paused", Toast.LENGTH_SHORT).show();
                break;

            case PAUSED:
                mediaRecorder.resume();
                mediaRecorderState = MediaRecorderState.RECORDING;
                btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_pause_black_24dp));
                btnRecordPause.setEnabled(true);
                btnStop.setEnabled(true);
                Toast.makeText(activity, "Recording Resumed", Toast.LENGTH_SHORT).show();
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
                    mediaRecorder.setAudioSamplingRate(16000);
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaRecorderState = MediaRecorderState.RECORDING;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_pause_black_24dp));
                    btnRecordPause.setEnabled(true);
                } else {
                    btnRecordPause.setEnabled(false);
                }

                btnStop.setEnabled(true);
                Toast.makeText(activity, "Recording Started", Toast.LENGTH_SHORT).show();

            default:
                break;
        }
    }

    public void stopRecording() {
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaRecorder = null;

        btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_mic_black_24dp));
        btnRecordPause.setEnabled(true);
        btnStop.setEnabled(false);
        mediaRecorderState = MediaRecorderState.STOPPED;

        FileListFragment fileListFragment = (FileListFragment) activity.getSupportFragmentManager().findFragmentById(R.id.list_fragment_container);
        if (fileListFragment != null) {
            fileListFragment.refreshAdapter();
        }

        Toast.makeText(activity, "Recording Saved Successfully", Toast.LENGTH_SHORT).show();
    }

}
