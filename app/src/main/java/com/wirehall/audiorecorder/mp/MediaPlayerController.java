package com.wirehall.audiorecorder.mp;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.visualizer.VisualizerFragment;

import java.io.IOException;


/**
 * This is a singleton class for controlling the media player operations
 */
public class MediaPlayerController {
    private static final String TAG = MediaPlayerController.class.getName();

    private static MediaPlayerController mediaPlayerController;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    private MediaPlayerController() {
        // Private Constructor
    }

    public static MediaPlayerController getInstance() {
        if (mediaPlayerController == null) {
            mediaPlayerController = new MediaPlayerController();
        }
        return mediaPlayerController;
    }

    public void playAudio(AppCompatActivity activity, String audioFilePath) {
        try {
            Log.d(TAG, "Playing audio file: " + audioFilePath);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            setMPVisualizerView(activity);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPlaying() {
        mediaPlayer.stop();
    }

    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

    private void setMPVisualizerView(AppCompatActivity activity) {
        VisualizerFragment visualizerFragment = (VisualizerFragment) activity.getSupportFragmentManager().findFragmentById(R.id.visualizer_fragment_container);
        if (visualizerFragment != null) {
            visualizerFragment.setMPVisualizerView();
        }
    }
}
