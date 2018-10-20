package com.wirehall.audiorecorder;

import android.annotation.TargetApi;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wirehall.audiorecorder.explorer.FileListFragment;
import com.wirehall.audiorecorder.explorer.FileUtils;
import com.wirehall.audiorecorder.visualizer.RecorderVisualizerView;
import com.wirehall.audiorecorder.visualizer.Utils;
import com.wirehall.audiorecorder.visualizer.VisualizerFragment;

import java.io.IOException;

/**
 * This is a singleton class for controlling the recording operations
 */
public class RecordingController {
    private static final String TAG = RecordingController.class.getName();
    private RecorderVisualizerView recorderVisualizerView;

    private static RecordingController recordingController;
    private Handler handler = new Handler();

    private AppCompatActivity activity;
    private MediaRecorder mediaRecorder;
    public static MediaRecorderState MEDIA_REC_STATE = MediaRecorderState.STOPPED;
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

        recorderVisualizerView = Utils.getRecorderVisualizerView(activity.getApplicationContext());


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
        switch (MEDIA_REC_STATE) {
            case RECORDING:
                mediaRecorder.pause();
                MEDIA_REC_STATE = MediaRecorderState.PAUSED;
                btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_mic_black_24dp));
                btnRecordPause.setEnabled(true);
                btnStop.setEnabled(true);
                Toast.makeText(activity, "Recording Paused", Toast.LENGTH_SHORT).show();
                break;

            case PAUSED:
                mediaRecorder.resume();
                MEDIA_REC_STATE = MediaRecorderState.RECORDING;
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

                MEDIA_REC_STATE = MediaRecorderState.RECORDING;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_pause_black_24dp));
                    btnRecordPause.setEnabled(true);
                } else {
                    btnRecordPause.setEnabled(false);
                }

                btnStop.setEnabled(true);

                VisualizerFragment visualizerFragment = (VisualizerFragment) activity.getSupportFragmentManager().findFragmentById(R.id.visualizer_fragment_container);
                if (visualizerFragment != null) {
                    visualizerFragment.addReplaceView(recorderVisualizerView);
                    handler.post(updateVisualizer);
                }

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
        MEDIA_REC_STATE = MediaRecorderState.STOPPED;

        FileListFragment fileListFragment = (FileListFragment) activity.getSupportFragmentManager().findFragmentById(R.id.list_fragment_container);
        if (fileListFragment != null) {
            fileListFragment.refreshAdapter();
        }
        Toast.makeText(activity, "Recording Saved Successfully", Toast.LENGTH_SHORT).show();
    }

    public void releaseRecorder() {
        if (mediaRecorder != null) {
            MEDIA_REC_STATE = MediaRecorderState.STOPPED;
            handler.removeCallbacks(updateVisualizer);
            recorderVisualizerView.clear();
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    // updates the visualizer every 50 milliseconds
    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (MEDIA_REC_STATE == MediaRecorderState.RECORDING) // if we are already recording
            {
                // get the current amplitude
                int x = mediaRecorder.getMaxAmplitude();
                recorderVisualizerView.addAmplitude(x); // update the VisualizeView
                recorderVisualizerView.invalidate(); // refresh the VisualizerView

                // update in few milliseconds
                handler.postDelayed(this, 40);
            }
        }
    };
}
