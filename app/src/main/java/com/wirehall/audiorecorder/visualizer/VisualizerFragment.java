package com.wirehall.audiorecorder.visualizer;

import android.content.Context;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.visualizer.view.BaseVisualizerView;
import com.wirehall.audiorecorder.visualizer.view.RecorderVisualizerView;

import java.util.List;

public class VisualizerFragment extends Fragment implements OnClickListener {
    private View currentVisualizerView;
    private VisualizerMPSession activity;
    private LinearLayout visualizerLayout;
    private Visualizer visualizer;
    private List<BaseVisualizerView> visualizerViews;
    private int visualizerViewIndex = -1;

    public VisualizerFragment() {
        // Required empty public constructor
    }

    public static VisualizerFragment newInstance() {
        return new VisualizerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        visualizerLayout = (LinearLayout) inflater.inflate(R.layout.visualizer_fragment, container, false);
        visualizerLayout.setOnClickListener(this);
        visualizerViews = Utils.getAllMPVisualizerViews(getContext());
        visualizerViewIndex = 0;
        setMPVisualizerView();

        return visualizerLayout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (VisualizerMPSession) context;
    }

    @Override
    public void onClick(View view) {
        if (currentVisualizerView instanceof RecorderVisualizerView) {
            return;
        }

        visualizerViewIndex++;
        visualizerViewIndex = visualizerViewIndex % visualizerViews.size();

        BaseVisualizerView baseVisualizerView = visualizerViews.get(visualizerViewIndex);
        setBaseVisualizerViewUpdater(baseVisualizerView, activity.getAudioSessionIdOfMediaPlayer());
        addReplaceView(baseVisualizerView);
    }

    public void addReplaceView(View view) {
        currentVisualizerView = view;
        visualizerLayout.removeAllViews();
        visualizerLayout.addView(view);
    }

    public void setMPVisualizerView() {
        BaseVisualizerView baseVisualizerView = visualizerViews.get(visualizerViewIndex);
        setBaseVisualizerViewUpdater(baseVisualizerView, activity.getAudioSessionIdOfMediaPlayer());
        addReplaceView(baseVisualizerView);
    }

    private void setBaseVisualizerViewUpdater(final BaseVisualizerView baseVisualizerView, int audioSessionId) {
        releaseVisualizer();
        visualizer = new Visualizer(audioSessionId);
        visualizer.setEnabled(false);
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                baseVisualizerView.setBytes(bytes);
                baseVisualizerView.invalidate();
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);

        visualizer.setEnabled(true);
    }

    public void releaseVisualizer() {
        if (visualizer != null) {
            visualizer.setEnabled(false);
            visualizer.release();
        }
    }

    public void removeAllViews() {
        visualizerLayout.removeAllViews();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseVisualizer();
    }

    public interface VisualizerMPSession {
        int getAudioSessionIdOfMediaPlayer();
    }
}
