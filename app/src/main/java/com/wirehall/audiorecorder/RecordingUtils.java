package com.wirehall.audiorecorder;

import android.media.MediaPlayer;

import java.io.IOException;

public class RecordingUtils {
    public static void playAudio(String audioFilePath, MediaPlayer mediaPlayer) {
        try {
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
}
