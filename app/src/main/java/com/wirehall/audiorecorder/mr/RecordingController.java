package com.wirehall.audiorecorder.mr;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.TextView;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.explorer.FileListFragment;
import com.wirehall.audiorecorder.visualizer.VisualizerFragment;
import com.wirehall.audiorecorder.visualizer.view.RecorderVisualizerView;

import static com.wirehall.audiorecorder.mr.AudioRecorderLocalService.ACTION_PAUSE_RECORDING;
import static com.wirehall.audiorecorder.mr.AudioRecorderLocalService.ACTION_RESUME_RECORDING;
import static com.wirehall.audiorecorder.mr.AudioRecorderLocalService.ACTION_START_RECORDING;
import static com.wirehall.audiorecorder.mr.AudioRecorderLocalService.ACTION_STOP_RECORDING;

/**
 * Singleton class for controlling the recording operations
 */
public class RecordingController {
    private static final String TAG = RecordingController.class.getName();
    private static RecordingController recordingController;
    private final Handler handler = new Handler();

    private Runnable visualizerRunnable;
    private boolean isActivityStopped = false;

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
        mapUIToState(activity);
    }

    /**
     * Start or pause the audio recording based on the last state.
     *
     * @param activity Activity required for internal operations
     */
    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startPauseRecording(AppCompatActivity activity) {
        Intent serviceIntent = new Intent(activity, AudioRecorderLocalService.class);

        switch (AudioRecorderLocalService.MEDIA_REC_STATE) {
            case RECORDING:
            case RESUMED:
                // Pause the Recording
                serviceIntent.setAction(ACTION_PAUSE_RECORDING);
                activity.startService(serviceIntent);
                break;

            case PAUSED:
                // Resume the Recording
                serviceIntent.setAction(ACTION_RESUME_RECORDING);
                activity.startService(serviceIntent);
                break;

            case STOPPED:
            case DISCARDED:
                // Start the Recording
                serviceIntent.setAction(ACTION_START_RECORDING);
                activity.startService(serviceIntent);
                break;

            default:
                break;
        }
    }

    public void onRecordingStarted(AppCompatActivity activity) {
        mapUIToState(activity);
    }

    public void onRecordingPaused(AppCompatActivity activity) {
        mapUIToState(activity);
    }

    public void onRecordingResumed(AppCompatActivity activity) {
        mapUIToState(activity);
    }

    public void onRecordingStopped(AppCompatActivity activity, boolean isDelete) {
        mapUIToState(activity);

        if (!isDelete) {
            refreshFileListView(activity);
        }
    }

    /**
     * Stop the audio recording
     *
     * @param activity Activity required for internal operations
     * @param isDelete Indicates if the current active recording is to be deleted
     */
    public void stopRecordingViaService(AppCompatActivity activity, boolean isDelete) {
        Intent serviceIntent = new Intent(activity, AudioRecorderLocalService.class);
        serviceIntent.setAction(ACTION_STOP_RECORDING);
        serviceIntent.putExtra("isDelete", isDelete);
        activity.startService(serviceIntent);
    }

    private void mapUIToState(AppCompatActivity activity) {
        ImageButton btnRecordPause, btnDelete, btnStop;
        btnRecordPause = activity.findViewById(R.id.ib_record);
        btnDelete = activity.findViewById(R.id.ib_delete);
        btnStop = activity.findViewById(R.id.ib_stop);

        switch (AudioRecorderLocalService.MEDIA_REC_STATE) {
            case RECORDING:
                // New recording
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_pause_white));
                    btnRecordPause.setEnabled(true);
                } else {
                    btnRecordPause.setEnabled(false);
                }

                btnStop.setEnabled(true);
                btnDelete.setEnabled(true);
                addRecorderVisualizerView(activity);
                break;
            case RESUMED:
                // Resumed recording
                btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_pause_white));
                btnRecordPause.setEnabled(true);
                btnStop.setEnabled(true);
                btnDelete.setEnabled(true);
                addRecorderVisualizerView(activity);
                break;
            case PAUSED:
                btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_mic));
                btnRecordPause.setEnabled(true);
                btnStop.setEnabled(true);
                break;
            case STOPPED:
            case DISCARDED:
                btnRecordPause.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_mic));
                btnRecordPause.setEnabled(true);
                btnStop.setEnabled(false);
                btnDelete.setEnabled(false);

                resetRunnable();
                VisualizerFragment visualizerFragment = (VisualizerFragment) activity.getSupportFragmentManager().findFragmentById(R.id.visualizer_fragment_container);
                if (visualizerFragment != null) {
                    visualizerFragment.getRecorderVisualizerView().clear();
                }
                TextView timerTextView = activity.findViewById(R.id.tv_timer);
                timerTextView.setText("");
                break;
            default:
                break;
        }
    }

    public void onDestroy() {
        resetRunnable();
    }

    private void resetRunnable() {
        handler.removeCallbacks(visualizerRunnable);
        visualizerRunnable = null;
    }

    public void onActivityStarted() {
        isActivityStopped = false;
    }

    public void onActivityStopped() {
        isActivityStopped = true;
    }

    private void addRecorderVisualizerView(AppCompatActivity activity) {
        VisualizerFragment visualizerFragment = (VisualizerFragment) activity.getSupportFragmentManager().findFragmentById(R.id.visualizer_fragment_container);
        if (visualizerFragment != null) {
            visualizerFragment.setRecorderVisualizerView();
            TextView timerTextView = activity.findViewById(R.id.tv_timer);
            setVisualizerRunnable(activity, timerTextView, visualizerFragment.getRecorderVisualizerView());
            handler.post(visualizerRunnable);
        }
    }

    private void refreshFileListView(AppCompatActivity activity) {
        FileListFragment fileListFragment = (FileListFragment) activity.getSupportFragmentManager().findFragmentById(R.id.list_fragment_container);
        if (fileListFragment != null) {
            fileListFragment.refreshAdapter();
        }
    }

    // updates the visualizer every few milliseconds
    private void setVisualizerRunnable(final Context context,
                                       final TextView timerTextView, final RecorderVisualizerView recorderVisualizerView) {
        visualizerRunnable = new Runnable() {
            @Override
            public void run() {
                if (AudioRecorderLocalService.mediaRecorder != null && AudioRecorderLocalService.MEDIA_REC_STATE.isRecording()
                        && !isActivityStopped) {
                    int x = AudioRecorderLocalService.mediaRecorder.getMaxAmplitude();  // get the current amplitude
                    recorderVisualizerView.addAmplitude(x); // update the VisualizeView
                    recorderVisualizerView.invalidate(); // refresh the VisualizerView

                    long totalRecTime = AudioRecorderLocalService.recordingTime.autoSetTotalRecTime();
                    int secs = (int) (totalRecTime / 1000);
                    int minutes = secs / 60;
                    secs = secs % 60;
                    timerTextView.setText(context.getResources().getString(R.string.duration_in_min_sec_short, minutes, secs));
                    // update in few milliseconds

                    handler.postDelayed(this, 40);
                }
            }
        };
    }


}