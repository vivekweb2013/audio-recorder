package com.wirehall.audiorecorder;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.wirehall.audiorecorder.explorer.FileListFragment;
import com.wirehall.audiorecorder.explorer.model.Recording;
import com.wirehall.audiorecorder.player.MediaPlayerController;
import com.wirehall.audiorecorder.recorder.AudioRecorderLocalService;
import com.wirehall.audiorecorder.recorder.MediaRecorderState;
import com.wirehall.audiorecorder.recorder.RecordingController;
import com.wirehall.audiorecorder.visualizer.VisualizerFragment;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity
    implements VisualizerFragment.VisualizerMPSession,
        FileListFragment.FileListFragmentListener,
        NavigationView.OnNavigationItemSelectedListener {

  public static final String KEY_PREF_RECORDING_STORAGE_PATH = "recording_storage_path";
  private static final String TAG = MainActivity.class.getName();

  private final ActivityResultLauncher<String[]> requestPermissionLauncher =
      registerForActivityResult(
          new ActivityResultContracts.RequestMultiplePermissions(),
          permissions -> {
            for (boolean isGranted : permissions.values()) {
              if (!isGranted) {
                finish();
              }
            }
          });

  private final RecordingController recordingController = RecordingController.getInstance();
  private final MediaPlayerController mediaPlayerController = MediaPlayerController.getInstance();
  private BroadcastReceiver broadcastReceiver;
  private boolean isServiceBound = false;

  /** Defines callbacks for service binding, passed to bindService() */
  private final ServiceConnection serviceConnection =
      new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
          // We've bound to LocalService, cast the IBinder and get LocalService instance
          isServiceBound = true;

          // This will be invoked if actions are performed via service notification
          // So that the activity can update the UI accordingly
          broadcastReceiver =
              new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                  MediaRecorderState mediaRecState = AudioRecorderLocalService.mediaRecorderState;
                  String recordingFilePath =
                      intent.getStringExtra(AudioRecorderLocalService.KEY_RECORDING_FILE_PATH);
                  switch (mediaRecState) {
                    case RECORDING:
                      recordingController.onRecordingStarted(MainActivity.this);
                      break;
                    case RESUMED:
                      recordingController.onRecordingResumed(MainActivity.this);
                      break;
                    case PAUSED:
                      recordingController.onRecordingPaused(MainActivity.this);
                      break;
                    case STOPPED:
                      recordingController.onRecordingStopped(
                          MainActivity.this, false, recordingFilePath);
                      break;
                    case DISCARDED:
                      recordingController.onRecordingStopped(
                          MainActivity.this, true, recordingFilePath);
                      break;
                    default:
                      break;
                  }
                }
              };
          LocalBroadcastManager.getInstance(getBaseContext())
              .registerReceiver(
                  broadcastReceiver,
                  new IntentFilter(AudioRecorderLocalService.EVENT_RECORDER_STATE_CHANGE));
          recordingController.init(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
          isServiceBound = false;
          try {
            // https://stackoverflow.com/questions/2682043/how-to-check-if-receiver-is-registered-in-android
            LocalBroadcastManager.getInstance(getBaseContext())
                .unregisterReceiver(broadcastReceiver);
          } catch (Exception e) {
            Log.e(TAG, e.getMessage());
          }
          broadcastReceiver = null;
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {
      setContentView(R.layout.main_activity);
      setupNavDrawer();

      FragmentManager fm = getSupportFragmentManager();
      FragmentTransaction ft = fm.beginTransaction();
      ft.add(R.id.visualizer_fragment_container, VisualizerFragment.newInstance());
      ft.commit();

      if (!HelperUtils.hasPermissions(getApplicationContext(), RECORD_AUDIO, WRITE_EXTERNAL_STORAGE)) {
        requestPermissionLauncher.launch(new String[] {RECORD_AUDIO, WRITE_EXTERNAL_STORAGE});
      }

      mediaPlayerController.init(this);

      setDefaultPreferenceValues();
      AppRater.launchIfRequired(this);
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
  }

  private void setupNavDrawer() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar); // set the toolbar

    DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
    NavigationView navView = findViewById(R.id.nav_view);
    navView.setItemIconTintList(null);

    ActionBarDrawerToggle toggle =
        new ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.nav_drawer_open_description,
            R.string.nav_drawer_close_description);
    drawerLayout.addDrawerListener(toggle);
    toggle.syncState();
    navView.setNavigationItemSelectedListener(this);
  }

  private void setDefaultPreferenceValues() {
    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    if (prefs.getString(KEY_PREF_RECORDING_STORAGE_PATH, null) == null) {
      SharedPreferences.Editor editor = prefs.edit();
      editor.putString(KEY_PREF_RECORDING_STORAGE_PATH, FileListFragment.DEFAULT_STORAGE_PATH);
      editor.apply();
    }
  }

  @Override
  protected void onStart() {
    try {
      Intent intent = new Intent(this, AudioRecorderLocalService.class);
      bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
    super.onStart();
  }

  /** @param view The method is the click handler for recorder delete button */
  public void deleteBtnClicked(View view) {
    try {
      recordingController.stopRecordingViaService(this, true);
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
  }

  /** @param view The method is the click handler for recorder stop button */
  public void stopBtnClicked(View view) {
    try {
      recordingController.stopRecordingViaService(this, false);
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
  }

  @Override
  public int getAudioSessionIdOfMediaPlayer() {
    return mediaPlayerController.getAudioSessionId();
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();

    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();
    ft.add(R.id.list_fragment_container, FileListFragment.newInstance());
    ft.commit();
  }

  @TargetApi(Build.VERSION_CODES.N)
  @RequiresApi(api = Build.VERSION_CODES.N)
  public void recordPauseBtnClicked(View view) {
    try {
      mediaPlayerController.stopPlaying(this);
      if (!isServiceBound) return;
      recordingController.startPauseRecording(this);
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
  }

  @Override
  public void onFileItemClicked(Recording recording) {
    try {
      if (!AudioRecorderLocalService.mediaRecorderState.isStopped()) {
        Toast.makeText(
                getApplicationContext(),
                getResources().getString(R.string.warn_stop_rec_to_play_audio),
                Toast.LENGTH_SHORT)
            .show();
        return;
      }
      mediaPlayerController.playPauseAudio(this, recording);
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
  }

  @Override
  protected void onStop() {
    try {
      if (isServiceBound) unbindService(serviceConnection);
      isServiceBound = false;
      // https://stackoverflow.com/questions/2682043/how-to-check-if-receiver-is-registered-in-android
      LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }

    super.onStop();
  }

  @Override
  protected void onDestroy() {
    try {
      mediaPlayerController.releaseMediaPlayer();
      recordingController.onDestroy();
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
    super.onDestroy();
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
    int itemId = item.getItemId();

    if (itemId == R.id.nav_settings) {
      HelperUtils.openSettings(this);
    } else if (itemId == R.id.nav_privacy_policy) {
      HelperUtils.openPrivacyPolicyIntent(this);
    } else if (itemId == R.id.nav_source_code) {
      HelperUtils.openSourceCodeIntent(this);
    } else if (itemId == R.id.nav_bug_report) {
      HelperUtils.openBugReportIntent(this);
    } else if (itemId == R.id.nav_rate) {
      HelperUtils.openRateIntent(this);
    } else if (itemId == R.id.nav_twitter) {
      HelperUtils.openTwitterIntent(this);
    } else if (itemId == R.id.nav_about) {
      HelperUtils.openAboutDialog(this);
    }

    drawerLayout.closeDrawer(GravityCompat.START);
    return false;
  }
}
