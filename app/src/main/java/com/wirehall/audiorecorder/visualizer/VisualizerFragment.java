package com.wirehall.audiorecorder.visualizer;

import android.content.Context;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.visualizer.view.BaseVisualizerView;
import com.wirehall.audiorecorder.visualizer.view.RecorderVisualizerView;

import java.util.List;

public class VisualizerFragment extends Fragment implements OnClickListener {
  private View currentVisualizerView;
  private VisualizerMPSession activity;
  private LinearLayout visualizerLayout;
  private Visualizer visualizer;
  private List<BaseVisualizerView> mpVisualizerViews;
  private RecorderVisualizerView recorderVisualizerView;
  private int visualizerViewIndex = -1;

  public VisualizerFragment() {
    // Required empty public constructor
  }

  public static VisualizerFragment newInstance() {
    return new VisualizerFragment();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    visualizerLayout =
        (LinearLayout) inflater.inflate(R.layout.visualizer_fragment, container, false);
    visualizerLayout.setOnClickListener(this);
    mpVisualizerViews = VisualizerUtils.getAllMPVisualizerViews(getContext());
    recorderVisualizerView = VisualizerUtils.getRecorderVisualizerView(getContext());
    visualizerViewIndex = 0;
    setMPVisualizerView();

    return visualizerLayout;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    activity = (VisualizerMPSession) context;
  }

  @Override
  public void onClick(View view) {
    if (currentVisualizerView instanceof RecorderVisualizerView) {
      return;
    }

    visualizerViewIndex++;
    visualizerViewIndex = visualizerViewIndex % mpVisualizerViews.size();

    BaseVisualizerView baseVisualizerView = mpVisualizerViews.get(visualizerViewIndex);
    setBaseVisualizerViewUpdater(baseVisualizerView, activity.getAudioSessionIdOfMediaPlayer());
    addReplaceView(baseVisualizerView);
  }

  /**
   * @param view Add a view to visualizer container Or replace if visualizer container already has a
   *     view
   */
  public void addReplaceView(View view) {
    currentVisualizerView = view;
    visualizerLayout.removeAllViews();
    visualizerLayout.addView(view);
  }

  public View getCurrentView() {
    return currentVisualizerView;
  }

  /**
   * Sets the Media Player Visualizer view to visualizer container The view pointed by the view
   * index is set
   */
  public void setMPVisualizerView() {
    BaseVisualizerView baseVisualizerView = mpVisualizerViews.get(visualizerViewIndex);
    setBaseVisualizerViewUpdater(baseVisualizerView, activity.getAudioSessionIdOfMediaPlayer());
    addReplaceView(baseVisualizerView);
  }

  public void setRecorderVisualizerView() {
    addReplaceView(recorderVisualizerView);
  }

  public RecorderVisualizerView getRecorderVisualizerView() {
    return recorderVisualizerView;
  }

  private void setBaseVisualizerViewUpdater(
      final BaseVisualizerView baseVisualizerView, int audioSessionId) {
    releaseVisualizer();

    if (audioSessionId > 0) {
      visualizer = new Visualizer(audioSessionId);
      visualizer.setEnabled(false);
      visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

      visualizer.setDataCaptureListener(
          new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(
                Visualizer visualizer, byte[] bytes, int samplingRate) {
              baseVisualizerView.setBytes(bytes);
              baseVisualizerView.invalidate();
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
              // No implementation required
            }
          },
          Visualizer.getMaxCaptureRate() / 2,
          true,
          false);

      visualizer.setEnabled(true);
    }
  }

  /** Release the visualizer */
  public void releaseVisualizer() {
    if (visualizer != null) {
      visualizer.setEnabled(false);
      visualizer.release();
    }
  }

  /** Removes all the views from visualizer container */
  public void removeAllViews() {
    visualizerLayout.removeAllViews();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    releaseVisualizer();
  }

  /**
   * Interface used to get the media player session from the activity The media player session is
   * required for initializing the visualizer
   */
  public interface VisualizerMPSession {
    int getAudioSessionIdOfMediaPlayer();
  }
}
