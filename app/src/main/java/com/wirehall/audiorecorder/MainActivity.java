package com.wirehall.audiorecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.wirehall.audiorecorder.explorer.FileListFragment;
import com.wirehall.audiorecorder.explorer.model.Recording;
import com.wirehall.audiorecorder.mp.MediaPlayerController;
import com.wirehall.audiorecorder.mr.AudioRecorderLocalService;
import com.wirehall.audiorecorder.mr.MediaRecorderState;
import com.wirehall.audiorecorder.mr.RecordingController;
import com.wirehall.audiorecorder.setting.SettingActivity;
import com.wirehall.audiorecorder.visualizer.VisualizerFragment;

public class MainActivity extends AppCompatActivity implements VisualizerFragment.VisualizerMPSession, FileListFragment.FileListFragmentListener {
    private static final String TAG = MainActivity.class.getName();

    public final static String APP_PACKAGE_NAME = "com.wirehall.audiorecorder";
    public static final String KEY_PREF_RECORDING_STORAGE_PATH = "recording_storage_path";
    private static final String PLAY_STORE_URL = "market://details?id=" + APP_PACKAGE_NAME;

    private static final int PERMISSION_REQUEST_CODE = 111;
    private static final String[] APP_PERMS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final RecordingController recordingController = RecordingController.getInstance();
    private final MediaPlayerController mediaPlayerController = MediaPlayerController.getInstance();
    private AudioRecorderLocalService audioRecorderLocalService;
    private BroadcastReceiver broadcastReceiver;
    private boolean isServiceBound = false;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AudioRecorderLocalService.LocalBinder binder = (AudioRecorderLocalService.LocalBinder) service;
            audioRecorderLocalService = binder.getService();
            isServiceBound = true;

            // This will be invoked if actions are performed via service notification
            // So that the activity can update the UI accordingly
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    MediaRecorderState MEDIA_REC_STATE = AudioRecorderLocalService.MEDIA_REC_STATE;
                    switch (MEDIA_REC_STATE) {
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
                            recordingController.onRecordingStopped(MainActivity.this, false);
                            break;
                        case DISCARDED:
                            recordingController.onRecordingStopped(MainActivity.this, true);
                            break;
                        default:
                            break;
                    }
                }
            };
            LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(broadcastReceiver, new IntentFilter("RecorderStateChange"));
            recordingController.init(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
            try {
                // https://stackoverflow.com/questions/2682043/how-to-check-if-receiver-is-registered-in-android
                LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            broadcastReceiver = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.visualizer_fragment_container, VisualizerFragment.newInstance());
        ft.commit();
        ActivityCompat.requestPermissions(this, APP_PERMS, PERMISSION_REQUEST_CODE);
        mediaPlayerController.init(this);

        setDefaultPreferenceValues();
        AppRater.launchIfRequired(this);
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
        Intent intent = new Intent(this, AudioRecorderLocalService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        recordingController.onActivityStarted();
        super.onStart();
    }

    /**
     * @param view The method is the click handler for recorder delete button
     */
    public void deleteBtnClicked(View view) {
        recordingController.stopRecordingViaService(this, true);
    }

    /**
     * @param view The method is the click handler for recorder stop button
     */
    public void stopBtnClicked(View view) {
        recordingController.stopRecordingViaService(this, false);
    }

    @Override
    public int getAudioSessionIdOfMediaPlayer() {
        return mediaPlayerController.getAudioSessionId();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isPermissionAccepted = false;
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                for (int result : grantResults) {
                    isPermissionAccepted = (result == PackageManager.PERMISSION_GRANTED);
                    if (!isPermissionAccepted)
                        break;
                }
                break;
        }
        if (isPermissionAccepted) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.list_fragment_container, FileListFragment.newInstance());
            ft.commit();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_item_settings:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_item_about:
                AboutDialog aboutDialog = new AboutDialog(this);
                aboutDialog.show();
                return true;
            case R.id.menu_item_rate:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL)));
                if (editor != null) {
                    editor.putBoolean(AppRater.KEY_PREF_RATE_DIALOG_DONT_SHOW, true);
                    editor.apply();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void recordPauseBtnClicked(View view) {
        mediaPlayerController.stopPlaying(this);
        if (!isServiceBound) return;
        recordingController.startPauseRecording(this);
    }

    @Override
    public void onFileItemClicked(Recording recording) {
        if (!AudioRecorderLocalService.MEDIA_REC_STATE.isStopped()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.warn_stop_rec_to_play_audio), Toast.LENGTH_SHORT).show();
            return;
        }
        mediaPlayerController.playPauseAudio(this, recording);
    }

    @Override
    protected void onStop() {
        if (isServiceBound)
            unbindService(serviceConnection);
        isServiceBound = false;
        recordingController.onActivityStopped();
        recordingController.onDestroy();

        try {
            // https://stackoverflow.com/questions/2682043/how-to-check-if-receiver-is-registered-in-android
            LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        broadcastReceiver = null;

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mediaPlayerController.releaseMediaPlayer();
        recordingController.onDestroy();

        super.onDestroy();
    }
}
