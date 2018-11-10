package com.wirehall.audiorecorder.mp;

import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.explorer.FileListFragment;
import com.wirehall.audiorecorder.explorer.model.Recording;
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

    private Recording currentRecording = null;

    private MediaPlayerController() {
        // Private Constructor
    }

    /**
     * @return The singleton instance of MediaPlayerController
     */
    public static MediaPlayerController getInstance() {
        if (mediaPlayerController == null) {
            mediaPlayerController = new MediaPlayerController();
        }
        return mediaPlayerController;
    }

    /**
     * Initialize the MediaPlayerController
     *
     * @param activity Activity required for internal operations
     */
    public void init(final AppCompatActivity activity) {
        final TextView timerTextView = activity.findViewById(R.id.tv_timer);
        final SeekBar seekBar = activity.findViewById(R.id.sb_mp_seek_bar);
        seekBar.setEnabled(false);
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    if (!seekBar.isEnabled()) {
                        seekBar.setEnabled(true);
                    }
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
                onMediaHalt(activity);
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

    /**
     * Plays the audio file, also operates the seekbar
     *
     * @param activity     Activity required for internal operations
     * @param newRecording The media file
     */
    public void playPauseAudio(AppCompatActivity activity, Recording newRecording) {

        final SeekBar seekBar = activity.findViewById(R.id.sb_mp_seek_bar);
        try {
            if (currentRecording != null) {
                currentRecording.setPlaying(false);
            }
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                currentRecording = newRecording;
            } else if (mediaPlayer.isPlaying() && newRecording.equals(currentRecording)) {
                mediaPlayer.pause();
                currentRecording = newRecording;
                newRecording.setPlaying(false);
                return;
            } else if (!mediaPlayer.isPlaying() && mediaPlayer.getCurrentPosition() > 1 && newRecording.equals(currentRecording)) {
                mediaPlayer.start();
                currentRecording = newRecording;
                newRecording.setPlaying(true);
                return;
            } else {
                currentRecording = newRecording;
                releaseMediaPlayer();
                mediaPlayer = new MediaPlayer();
            }

            mediaPlayer.setOnCompletionListener(mPlayerOnCompletionListener);
            Log.d(TAG, "Playing audio file: " + newRecording.getPath());
            mediaPlayer.reset();
            mediaPlayer.setDataSource(newRecording.getPath());
            mediaPlayer.prepare();
            seekBar.setMax(0);
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setEnabled(true);
            mediaPlayer.start();
            newRecording.setPlaying(true);
            setMPVisualizerView(activity);

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "ERROR: IllegalArgumentException: " + e.getMessage());
        } catch (IllegalStateException e) {
            Log.e(TAG, "ERROR: IllegalStateException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "ERROR: IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "ERROR:  " + e.getMessage());
        }
    }

    /**
     * Stops the media playback
     *
     * @param activity Activity required for internal operations
     */
    public void stopPlaying(AppCompatActivity activity) {
        releaseMediaPlayer();
        onMediaHalt(activity);
    }

    // Performs the operations required after the audio play is halted
    private void onMediaHalt(AppCompatActivity activity) {
        final TextView timerTextView = activity.findViewById(R.id.tv_timer);
        final SeekBar seekBar = activity.findViewById(R.id.sb_mp_seek_bar);

        seekBar.setMax(0);
        seekBar.setProgress(0);
        seekBar.setEnabled(false);
        timerTextView.setText("");

        if (currentRecording != null)
            currentRecording.setPlaying(false);

        FileListFragment fileListFragment = (FileListFragment) activity.getSupportFragmentManager().findFragmentById(R.id.list_fragment_container);
        if (fileListFragment != null) {
            fileListFragment.resetRowSelection();
        }
    }

    /**
     * Release the media player instance
     */
    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * @return The audio session id of the media player instance
     */
    public int getAudioSessionId() {
        return mediaPlayer != null ? mediaPlayer.getAudioSessionId() : 0;
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
