package com.wirehall.audiorecorder.mp;

import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;

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

    private Handler handler = new Handler();

    private MediaPlayerController() {
        // Private Constructor
    }

    public static MediaPlayerController getInstance() {
        if (mediaPlayerController == null) {
            mediaPlayerController = new MediaPlayerController();
        }
        return mediaPlayerController;
    }

    public void init(AppCompatActivity activity) {
        final SeekBar seekBar = activity.findViewById(R.id.sb_mp_seek_bar);
        seekBar.setEnabled(false);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                seekBar.setMax(0);
                seekBar.setProgress(0);
                seekBar.setEnabled(false);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }
        });
    }

    public void playAudio(AppCompatActivity activity, String audioFilePath) {
        final SeekBar seekBar = activity.findViewById(R.id.sb_mp_seek_bar);
        try {
            Log.d(TAG, "Playing audio file: " + audioFilePath);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            seekBar.setMax(0);
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setEnabled(true);
            mediaPlayer.start();
            setMPVisualizerView(activity);

            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mediaPlayer.isPlaying()) {
                        seekBar.setMax(0);
                        seekBar.setMax(mediaPlayer.getDuration());
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        handler.postDelayed(this, 50);
                    }
                }
            });
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
