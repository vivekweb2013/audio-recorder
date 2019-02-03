package com.wirehall.audiorecorder.mr;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.wirehall.audiorecorder.MainActivity;
import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.explorer.FileUtils;
import com.wirehall.audiorecorder.setting.SettingActivity;

import java.io.IOException;

import static com.wirehall.audiorecorder.App.CHANNEL_ID;

public class AudioRecorderLocalService extends Service {
    private static final String TAG = AudioRecorderLocalService.class.getName();
    private static final int SERVICE_ID = 1;

    public static final String ACTION_START_RECORDING = "com.wirehall.audiorecorder.ACTION_START_RECORDING";
    public static final String ACTION_STOP_RECORDING = "com.wirehall.audiorecorder.ACTION_STOP_RECORDING";
    public static final String ACTION_PAUSE_RECORDING = "com.wirehall.audiorecorder.ACTION_PAUSE_RECORDING";
    public static final String ACTION_RESUME_RECORDING = "com.wirehall.audiorecorder.ACTION_RESUME_RECORDING";

    public static MediaRecorderState MEDIA_REC_STATE = MediaRecorderState.STOPPED;
    public static MediaRecorder mediaRecorder;
    public static RecordingTime recordingTime;

    private String recordingFilePath;
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public AudioRecorderLocalService getService() {
            // Return this instance of this service so clients can call public methods
            return AudioRecorderLocalService.this;
        }
    }

    @Override
    public void onCreate() {
        mediaRecorder = new MediaRecorder();
        recordingTime = new RecordingTime();
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return super.onStartCommand(null, flags, startId);

        switch (intent.getAction()) {
            case ACTION_START_RECORDING:
                Log.d(TAG, "Received Start Recording Intent ");
                Intent notificationIntent = new Intent(this, MainActivity.class);
                notificationIntent.setAction(Intent.ACTION_MAIN);
                notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                Intent stopRecordIntent = new Intent(this, AudioRecorderLocalService.class);
                stopRecordIntent.setAction(ACTION_STOP_RECORDING);
                PendingIntent stopRecordPendingIntent = PendingIntent.getService(this, 0, stopRecordIntent, 0);

                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(getBaseContext().getString(R.string.app_name))
                        .setContentText(getBaseContext().getString(R.string.recording_in_progress))
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .addAction(R.drawable.ic_stop_white, getBaseContext().getString(R.string.btn_stop_recording), stopRecordPendingIntent).build();

                startRecording(getBaseContext());
                broadcastRecorderStateChange();
                startForeground(SERVICE_ID, notification);
                break;
            case ACTION_STOP_RECORDING:
                Log.i(TAG, "Received Stop Recording Intent");
                try {
                    boolean isDelete = intent.getBooleanExtra("isDelete", false);
                    stopRecording(isDelete);
                    broadcastRecorderStateChange();
                } catch (Exception e) {
                    Log.e(TAG, "ERROR: " + e.getMessage());
                }
                stopForeground(true);
                stopSelf();
                break;
            case ACTION_PAUSE_RECORDING:
                Log.i(TAG, "Received Pause Foreground Intent");
                pauseRecording();
                broadcastRecorderStateChange();
                break;
            case ACTION_RESUME_RECORDING:
                Log.i(TAG, "Received Resume Foreground Intent");
                resumeRecording();
                broadcastRecorderStateChange();
                break;
            default:
                break;
        }

        return START_STICKY;
    }

    private void broadcastRecorderStateChange() {
        // broadcast state change so that the activity is notified
        // and it can make UI changes accordingly
        Intent recorderStateChangeIntent = new Intent("RecorderStateChange");
        LocalBroadcastManager.getInstance(this).sendBroadcast(recorderStateChangeIntent);
    }

    private void startRecording(Context context) {
        try {
            String recordingStoragePath = FileUtils.getRecordingStoragePath(context);
            recordingFilePath = recordingStoragePath + '/' + FileUtils.generateFileName();
            Log.d(TAG, "Recording Path: " + recordingFilePath);

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String audioQualityNormal = context.getResources().getString(R.string.audio_quality_normal);
            String audioQualityPref = sharedPref.getString(SettingActivity.KEY_PREF_LIST_AUDIO_QUALITY, audioQualityNormal);
            if (audioQualityPref != null && audioQualityPref.equals(audioQualityNormal)) {
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.setAudioSamplingRate(8000);
            } else {
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
                mediaRecorder.setAudioSamplingRate(16000);
            }
            mediaRecorder.setOutputFile(recordingFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            recordingTime.setRecStartTime(SystemClock.uptimeMillis());
            MEDIA_REC_STATE = MediaRecorderState.RECORDING;
            Toast.makeText(context, context.getString(R.string.message_recording_started), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "ERROR: IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "ERROR: " + e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pauseRecording() {
        mediaRecorder.pause();
        MEDIA_REC_STATE = MediaRecorderState.PAUSED;
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resumeRecording() {
        mediaRecorder.resume();
        recordingTime.autoSetRecPauseTime();
        MEDIA_REC_STATE = MediaRecorderState.RESUMED;
    }


    /**
     * Stop the audio recording
     *
     * @param isDelete Indicates if the current active recording is to be deleted
     */
    private void stopRecording(boolean isDelete) {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
        }
        if (isDelete) {
            FileUtils.deleteFile(recordingFilePath);
            MEDIA_REC_STATE = MediaRecorderState.DISCARDED;
        } else {
            Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.message_recording_saved), Toast.LENGTH_SHORT).show();
            MEDIA_REC_STATE = MediaRecorderState.STOPPED;
        }
        // Reset Timer
        recordingTime.reset();
    }

    @Override
    public void onDestroy() {
        try {
            if (mediaRecorder != null) {
                if (!MEDIA_REC_STATE.isStopped()) {
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                }
                mediaRecorder.release();
                mediaRecorder = null;
            }
            MEDIA_REC_STATE = MediaRecorderState.STOPPED;
        } catch (Exception e) {
            Log.e(TAG, "ERROR: " + e.getMessage());
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
