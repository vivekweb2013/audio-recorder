package com.wirehall.audiorecorder.mr;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.explorer.FileListFragment;
import com.wirehall.audiorecorder.explorer.FileUtils;
import com.wirehall.audiorecorder.setting.SettingActivity;
import com.wirehall.audiorecorder.visualizer.Utils;
import com.wirehall.audiorecorder.visualizer.VisualizerFragment;
import com.wirehall.audiorecorder.visualizer.view.RecorderVisualizerView;

import java.io.IOException;

/**
 * Singleton class for controlling the recording operations
 */
public class RecordingController {
    private static final String TAG = RecordingController.class.getName();
    private static RecordingController recordingController;
    private MediaRecorderState MEDIA_REC_STATE = MediaRecorderState.STOPPED;
    private MediaRecorder mediaRecorder;

    private Handler handler = new Handler();
    private RecorderVisualizerView recorderVisualizerView;
    private Runnable visualizerRunnable;

    private long totalRecTime = 0L;
    private long recStartTime = 0L;
    private long recPauseTime = 0L;

    private String recordingFilePath;

    private RecordingController() {
        // Private Constructor
    }

    public static RecordingController getInstance() {
        if (recordingController == null) {
            recordingController = new RecordingController();
        }
        return recordingController;
    }

    /**
     * Initialize the RecordingController
     *
     * @param activity Activity required for internal operations
     */
    public void init(AppCompatActivity activity) {

        ImageButton btnRecordPause = activity.findViewById(R.id.ib_record);
        ImageButton btnDelete = activity.findViewById(R.id.ib_delete);
        ImageButton btnStop = activity.findViewById(R.id.ib_stop);
        TextView timerTextView = activity.findViewById(R.id.tv_timer);

        if (btnRecordPause != null && btnDelete != null && btnStop != null) {
            btnRecordPause.setEnabled(true);
            btnDelete.setEnabled(false);
            btnStop.setEnabled(false);
        } else {
            Log.e(TAG, "some of the resources are not found! btnRecordPause:" + btnRecordPause + " btnDelete:" + btnDelete + " btnStop:" + btnStop);
        }

        setVisualizerRunnable(timerTextView);
    }

    /**
     * Start or pause the audio recording based on the last state.
     *
     * @param activity Activity required for internal operations
     */
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
                startRecording(activity, btnRecordPause, btnStop, btnDelete);

            default:
                break;
        }
    }

    private void startRecording(AppCompatActivity activity, ImageButton btnRecordPause, ImageButton btnStop, ImageButton btnDelete) {
        recordingFilePath = FileListFragment.STORAGE_PATH + '/' + FileUtils.generateFileName();
        Log.d("filename", recordingFilePath);

        initRecorder(activity);
        try {
            mediaRecorder.setOutputFile(recordingFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            recStartTime = SystemClock.uptimeMillis();
        } catch (IOException e) {
            Log.e(TAG, "ERROR: IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "ERROR: " + e.getMessage());
        }

        MEDIA_REC_STATE = MediaRecorderState.RECORDING;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_pause_black_24dp));
            btnRecordPause.setEnabled(true);
        } else {
            btnRecordPause.setEnabled(false);
        }
        btnStop.setEnabled(true);
        btnDelete.setEnabled(true);

        addRecorderVisualizerView(activity);

        Toast.makeText(activity, "Recording Started", Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resumeRecording(AppCompatActivity activity, ImageButton btnRecordPause, ImageButton btnStop) {
        mediaRecorder.resume();
        recPauseTime = SystemClock.uptimeMillis() - recStartTime - totalRecTime;
        MEDIA_REC_STATE = MediaRecorderState.RECORDING;
        btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_pause_black_24dp));
        btnRecordPause.setEnabled(true);
        btnStop.setEnabled(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pauseRecording(AppCompatActivity activity, ImageButton btnRecordPause, ImageButton btnStop) {
        mediaRecorder.pause();
        MEDIA_REC_STATE = MediaRecorderState.PAUSED;
        btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_mic_black_24dp));
        btnRecordPause.setEnabled(true);
        btnStop.setEnabled(true);
    }


    /**
     * Stop the audio recording
     *
     * @param activity Activity required for internal operations
     * @param isDelete Indicates if the current active recording is to be deleted
     */
    public void stopRecording(AppCompatActivity activity, boolean isDelete) {
        ImageButton btnRecordPause, btnDelete, btnStop;
        btnRecordPause = activity.findViewById(R.id.ib_record);
        btnDelete = activity.findViewById(R.id.ib_delete);
        btnStop = activity.findViewById(R.id.ib_stop);

        try {
            if (isDelete) {
                FileUtils.deleteFile(recordingFilePath);
            }
            removeRecorderVisualizerView(activity);
            releaseRecorder();
        } catch (Exception e) {
            Log.e(TAG, "ERROR: " + e.getMessage());
        }
        mediaRecorder = null;

        btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_mic_black_24dp));
        btnRecordPause.setEnabled(true);
        btnStop.setEnabled(false);
        btnDelete.setEnabled(false);
        MEDIA_REC_STATE = MediaRecorderState.STOPPED;

        if (!isDelete) {
            refreshFileListView(activity);
            Toast.makeText(activity, "Recording Saved Successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void initRecorder(Context context) {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String audioQualityPref = sharedPref.getString(SettingActivity.KEY_PREF_LIST_AUDIO_QUALITY, "NORMAL");
            if (audioQualityPref.equals("NORMAL")) {
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.setAudioSamplingRate(8000);
            } else {
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
                mediaRecorder.setAudioSamplingRate(16000);
            }
        }
    }

    /**
     * Release the recorder instance
     */
    public void releaseRecorder() {
        if (mediaRecorder != null) {
            MEDIA_REC_STATE = MediaRecorderState.STOPPED;
            handler.removeCallbacks(visualizerRunnable);
            recorderVisualizerView.clear();
            recorderVisualizerView = null;
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    /**
     * @return The current state of media recorder
     */
    public MediaRecorderState getMediaRecorderState() {
        return MEDIA_REC_STATE;
    }

    private void addRecorderVisualizerView(AppCompatActivity activity) {
        VisualizerFragment visualizerFragment = (VisualizerFragment) activity.getSupportFragmentManager().findFragmentById(R.id.visualizer_fragment_container);
        if (visualizerFragment != null) {
            recorderVisualizerView = Utils.getRecorderVisualizerView(activity.getApplicationContext());
            visualizerFragment.addReplaceView(recorderVisualizerView);
            handler.post(visualizerRunnable);
        }
    }

    private void removeRecorderVisualizerView(AppCompatActivity activity) {
        VisualizerFragment visualizerFragment = (VisualizerFragment) activity.getSupportFragmentManager().findFragmentById(R.id.visualizer_fragment_container);
        if (visualizerFragment != null) {
            visualizerFragment.removeAllViews();
        }

        // Reset Timer
        totalRecTime = 0;
        recPauseTime = 0;
        recStartTime = 0;
        TextView timerTextView = activity.findViewById(R.id.tv_timer);
        timerTextView.setText("");
    }


    private void refreshFileListView(AppCompatActivity activity) {
        FileListFragment fileListFragment = (FileListFragment) activity.getSupportFragmentManager().findFragmentById(R.id.list_fragment_container);
        if (fileListFragment != null) {
            fileListFragment.refreshAdapter();
        }
    }

    // updates the visualizer every few milliseconds
    private void setVisualizerRunnable(final TextView timerTextView) {
        visualizerRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaRecorder != null && recorderVisualizerView != null && MEDIA_REC_STATE == MediaRecorderState.RECORDING) {
                    int x = mediaRecorder.getMaxAmplitude();  // get the current amplitude
                    recorderVisualizerView.addAmplitude(x); // update the VisualizeView
                    recorderVisualizerView.invalidate(); // refresh the VisualizerView

                    totalRecTime = SystemClock.uptimeMillis() - recStartTime - recPauseTime;
                    int secs = (int) (totalRecTime / 1000);
                    int minutes = secs / 60;
                    secs = secs % 60;
                    timerTextView.setText("" + String.format("%02d", minutes) + ":" + String.format("%02d", secs));
                }
                // update in few milliseconds
                handler.postDelayed(this, 40);
            }
        };
    }

}
