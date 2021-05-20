package com.wirehall.audiorecorder.recorder;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.explorer.FileListFragment;
import com.wirehall.audiorecorder.explorer.FilenameInputDialog;
import com.wirehall.audiorecorder.setting.SettingActivity;
import com.wirehall.audiorecorder.visualizer.VisualizerFragment;
import com.wirehall.audiorecorder.visualizer.view.RecorderVisualizerView;

import static com.wirehall.audiorecorder.recorder.AudioRecorderLocalService.ACTION_PAUSE_RECORDING;
import static com.wirehall.audiorecorder.recorder.AudioRecorderLocalService.ACTION_RESUME_RECORDING;
import static com.wirehall.audiorecorder.recorder.AudioRecorderLocalService.ACTION_START_RECORDING;
import static com.wirehall.audiorecorder.recorder.AudioRecorderLocalService.ACTION_STOP_RECORDING;

/** Singleton class for controlling the recording operations */
public class RecordingController {
  private static final String TAG = RecordingController.class.getName();
  private static RecordingController recordingController;
  private final Handler handler = new Handler(Looper.myLooper());

  private Runnable visualizerRunnable;

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

    switch (AudioRecorderLocalService.mediaRecorderState) {
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

  public void onRecordingStopped(
      AppCompatActivity activity, boolean isDiscardRecording, String recordingFilePath) {
    mapUIToState(activity);

    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
    boolean requestFilename =
        sharedPref.getBoolean(SettingActivity.KEY_PREF_REQUEST_FILENAME, false);
    if (requestFilename && !isDiscardRecording) {
      launchAskForFilenameDialog(
          activity, recordingFilePath); // refreshFileListView called on dismiss
    } else if (!requestFilename && !isDiscardRecording) {
      refreshFileListView(activity);
      Toast.makeText(
              activity, activity.getString(R.string.message_recording_saved), Toast.LENGTH_SHORT)
          .show();
    }
  }

  /**
   * Stop the audio recording
   *
   * @param activity Activity required for internal operations
   * @param isDiscardRecording Indicates if the current active recording needs to be discarded
   */
  public void stopRecordingViaService(AppCompatActivity activity, boolean isDiscardRecording) {
    Intent serviceIntent = new Intent(activity, AudioRecorderLocalService.class);
    serviceIntent.setAction(ACTION_STOP_RECORDING);
    serviceIntent.putExtra(AudioRecorderLocalService.FLAG_IS_DISCARD_RECORDING, isDiscardRecording);
    activity.startService(serviceIntent);
  }

  private void mapUIToState(AppCompatActivity activity) {
    ImageButton btnRecordPause;
    ImageButton btnDelete;
    ImageButton btnStop;
    btnRecordPause = activity.findViewById(R.id.ib_record);
    btnDelete = activity.findViewById(R.id.ib_delete);
    btnStop = activity.findViewById(R.id.ib_stop);

    switch (AudioRecorderLocalService.mediaRecorderState) {
      case RECORDING:
        // New recording
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          btnRecordPause.setImageDrawable(
              ContextCompat.getDrawable(activity, R.drawable.ic_pause_white));
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
        btnRecordPause.setImageDrawable(
            ContextCompat.getDrawable(activity, R.drawable.ic_pause_white));
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
        VisualizerFragment visualizerFragment =
            (VisualizerFragment)
                activity
                    .getSupportFragmentManager()
                    .findFragmentById(R.id.visualizer_fragment_container);
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

  private void addRecorderVisualizerView(AppCompatActivity activity) {
    VisualizerFragment visualizerFragment =
        (VisualizerFragment)
            activity
                .getSupportFragmentManager()
                .findFragmentById(R.id.visualizer_fragment_container);
    if (visualizerFragment != null
        && (!(visualizerFragment.getCurrentView() instanceof RecorderVisualizerView)
            || visualizerRunnable == null)) {
      visualizerFragment.setRecorderVisualizerView();
      TextView timerTextView = activity.findViewById(R.id.tv_timer);
      setVisualizerRunnable(
          activity, timerTextView, visualizerFragment.getRecorderVisualizerView());
      handler.post(visualizerRunnable);
    }
  }

  private void refreshFileListView(AppCompatActivity activity) {
    FileListFragment fileListFragment =
        (FileListFragment)
            activity.getSupportFragmentManager().findFragmentById(R.id.list_fragment_container);
    if (fileListFragment != null) {
      fileListFragment.refreshAdapter();
    }
  }

  // updates the visualizer every few milliseconds
  private void setVisualizerRunnable(
      final Context context,
      final TextView timerTextView,
      final RecorderVisualizerView recorderVisualizerView) {
    visualizerRunnable =
        new Runnable() {
          @Override
          public void run() {
            if (AudioRecorderLocalService.mediaRecorder != null
                && AudioRecorderLocalService.mediaRecorderState.isRecording()) {
              int x =
                  AudioRecorderLocalService.mediaRecorder
                      .getMaxAmplitude(); // get the current amplitude
              recorderVisualizerView.addAmplitude(x); // update the VisualizeView
              recorderVisualizerView.invalidate(); // refresh the VisualizerView

              long totalRecTime = AudioRecorderLocalService.recordingTime.autoSetTotalRecTime();
              int secs = (int) (totalRecTime / 1000);
              int minutes = secs / 60;
              secs = secs % 60;
              timerTextView.setText(
                  context
                      .getResources()
                      .getString(R.string.duration_in_min_sec_short, minutes, secs));
            }
            // update in few milliseconds
            handler.postDelayed(this, 40);
          }
        };
  }

  public void launchAskForFilenameDialog(
      final AppCompatActivity activity, final String recordingFilePath) {
    DialogInterface.OnDismissListener onDismissListener =
        dialog -> {
          refreshFileListView(activity);
          Toast.makeText(
                  activity,
                  activity.getString(R.string.message_recording_saved),
                  Toast.LENGTH_SHORT)
              .show();
        };
    FilenameInputDialog filenameInputDialog = new FilenameInputDialog(activity, recordingFilePath);
    filenameInputDialog.setOnDismissListener(onDismissListener);
    filenameInputDialog.show();
  }
}
