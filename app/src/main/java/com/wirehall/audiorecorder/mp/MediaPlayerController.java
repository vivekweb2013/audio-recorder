package com.wirehall.audiorecorder.mp;

import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.visualizer.VisualizerFragment;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * This is a singleton class for controlling the media player operations
 */
public class MediaPlayerController {
    private static final String TAG = MediaPlayerController.class.getName();

    private static MediaPlayerController mediaPlayerController;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private MediaPlayer.OnCompletionListener mPlayerOnCompletionListener;

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
        final TextView timerTextView = activity.findViewById(R.id.tv_timer);
        final SeekBar seekBar = activity.findViewById(R.id.sb_mp_seek_bar);
        seekBar.setEnabled(false);
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setMax(0);
                    final int totalMediaDuration = mediaPlayer.getDuration();
                    seekBar.setMax(totalMediaDuration);
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    String currentPositionString = getFormattedTimeString(currentPosition);
                    final String totalMediaDurationString = getFormattedTimeString(totalMediaDuration);
                    String playbackTimerString = currentPositionString + "/" + totalMediaDurationString;
                    timerTextView.setText(playbackTimerString);
                    seekBar.setProgress(currentPosition);
                }
                handler.postDelayed(this, 50);
            }
        });
        mPlayerOnCompletionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                seekBar.setMax(0);
                seekBar.setProgress(0);
                seekBar.setEnabled(false);
                timerTextView.setText("");
            }
        };
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
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                releaseMediaPlayer();
                mediaPlayer = new MediaPlayer();
            } else if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            mediaPlayer.setOnCompletionListener(mPlayerOnCompletionListener);
            Log.d(TAG, "Playing audio file: " + audioFilePath);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            seekBar.setMax(0);
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setEnabled(true);
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

    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
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

    private String getFormattedTimeString(int duration) {
        String durationStr = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration), TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
        return durationStr;
    }
}
