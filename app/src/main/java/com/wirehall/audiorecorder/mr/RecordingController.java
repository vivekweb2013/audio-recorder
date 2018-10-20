package com.wirehall.audiorecorder.mr;

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

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.explorer.FileListFragment;
import com.wirehall.audiorecorder.explorer.FileUtils;
import com.wirehall.audiorecorder.visualizer.Utils;
import com.wirehall.audiorecorder.visualizer.VisualizerFragment;
import com.wirehall.audiorecorder.visualizer.view.RecorderVisualizerView;

import java.io.IOException;

/**
 * This is a singleton class for controlling the recording operations
 */
public class RecordingController {
    private static final String TAG = RecordingController.class.getName();
    private static RecordingController recordingController;
    private MediaRecorderState MEDIA_REC_STATE = MediaRecorderState.STOPPED;
    private MediaRecorder mediaRecorder;

    private Handler handler = new Handler();
    private RecorderVisualizerView recorderVisualizerView;

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
        ImageButton btnRecordPause, btnDelete, btnStop;

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
    public void startPauseRecording(AppCompatActivity activity) {
        ImageButton btnRecordPause, btnDelete, btnStop;

        btnRecordPause = activity.findViewById(R.id.ib_record);
        btnDelete = activity.findViewById(R.id.ib_delete);
        btnStop = activity.findViewById(R.id.ib_stop);

        switch (MEDIA_REC_STATE) {
            case RECORDING:
                pauseRecording(activity, btnRecordPause, btnStop);
                break;

            case PAUSED:
                resumeRecording(activity, btnRecordPause, btnStop);
                break;

            case STOPPED:
                startRecording(activity, btnRecordPause, btnStop);

            default:
                break;
        }
    }

    private void startRecording(AppCompatActivity activity, ImageButton btnRecordPause, ImageButton btnStop) {
        String fullFilePath = FileListFragment.STORAGE_PATH + '/' + FileUtils.generateFileName();
        Log.d("filename", fullFilePath);

        initRecorder();
        try {
            mediaRecorder.setOutputFile(fullFilePath);
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

        addRecorderVisualizerView(activity);

        Toast.makeText(activity, "Recording Started", Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resumeRecording(AppCompatActivity activity, ImageButton btnRecordPause, ImageButton btnStop) {
        mediaRecorder.resume();
        MEDIA_REC_STATE = MediaRecorderState.RECORDING;
        btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_pause_black_24dp));
        btnRecordPause.setEnabled(true);
        btnStop.setEnabled(true);
        Toast.makeText(activity, "Recording Resumed", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pauseRecording(AppCompatActivity activity, ImageButton btnRecordPause, ImageButton btnStop) {
        mediaRecorder.pause();
        MEDIA_REC_STATE = MediaRecorderState.PAUSED;
        btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_mic_black_24dp));
        btnRecordPause.setEnabled(true);
        btnStop.setEnabled(true);
        Toast.makeText(activity, "Recording Paused", Toast.LENGTH_SHORT).show();
    }


    public void stopRecording(AppCompatActivity activity) {
        ImageButton btnRecordPause, btnDelete, btnStop;

        btnRecordPause = activity.findViewById(R.id.ib_record);
        btnDelete = activity.findViewById(R.id.ib_delete);
        btnStop = activity.findViewById(R.id.ib_stop);

        try {
            removeRecorderVisualizerView(activity);
            releaseRecorder();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaRecorder = null;

        btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_mic_black_24dp));
        btnRecordPause.setEnabled(true);
        btnStop.setEnabled(false);
        MEDIA_REC_STATE = MediaRecorderState.STOPPED;

        refreshFileListView(activity);

        Toast.makeText(activity, "Recording Saved Successfully", Toast.LENGTH_SHORT).show();
    }


    private void initRecorder() {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setAudioSamplingRate(16000);
        }
    }

    public void releaseRecorder() {
        if (mediaRecorder != null) {
            MEDIA_REC_STATE = MediaRecorderState.STOPPED;
            handler.removeCallbacks(updateVisualizer);
            recorderVisualizerView.clear();
            recorderVisualizerView = null;
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    /**
     * @param activity This method creates and adds the recorderVisualizerView to visualizer fragment
     */
    private void addRecorderVisualizerView(AppCompatActivity activity) {
        VisualizerFragment visualizerFragment = (VisualizerFragment) activity.getSupportFragmentManager().findFragmentById(R.id.visualizer_fragment_container);
        if (visualizerFragment != null) {
            recorderVisualizerView = Utils.getRecorderVisualizerView(activity.getApplicationContext());
            visualizerFragment.addReplaceView(recorderVisualizerView);
            handler.post(updateVisualizer);
        }
    }

    /**
     * @param activity This method removes recorderVisualizerView from visualizer fragment
     */
    private void removeRecorderVisualizerView(AppCompatActivity activity) {
        VisualizerFragment visualizerFragment = (VisualizerFragment) activity.getSupportFragmentManager().findFragmentById(R.id.visualizer_fragment_container);
        if (visualizerFragment != null) {
            visualizerFragment.removeAllViews();
        }
    }


    private void refreshFileListView(AppCompatActivity activity) {
        FileListFragment fileListFragment = (FileListFragment) activity.getSupportFragmentManager().findFragmentById(R.id.list_fragment_container);
        if (fileListFragment != null) {
            fileListFragment.refreshAdapter();
        }
    }

    // updates the visualizer every few milliseconds
    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (mediaRecorder != null && recorderVisualizerView != null && MEDIA_REC_STATE == MediaRecorderState.RECORDING) {
                int x = mediaRecorder.getMaxAmplitude();  // get the current amplitude
                recorderVisualizerView.addAmplitude(x); // update the VisualizeView
                recorderVisualizerView.invalidate(); // refresh the VisualizerView
            }
            // update in few milliseconds
            handler.postDelayed(this, 40);
        }
    };
}
