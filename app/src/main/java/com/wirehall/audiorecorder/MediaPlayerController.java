package com.wirehall.audiorecorder;

import android.media.MediaPlayer;
import android.util.Log;

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

    public void playAudio(String audioFilePath) {
        try {
            Log.d(TAG, "Playing audio file: " + audioFilePath);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }
}
