package com.wirehall.audiorecorder.visualizer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.wirehall.audiorecorder.R;

import java.util.List;

public class VisualizerFragment extends Fragment implements OnClickListener {
    public interface VisualizerMPSession {
        int getAudioSessionIdOfMediaPlayer();
    }

    private View currentVisualizerView;

    private VisualizerMPSession activity;
    private LinearLayout visualizerLayout;

    private List<BaseVisualizer> visualizers;
    private int visualizerViewIndex = -1;

    public VisualizerFragment() {
        // Required empty public constructor
    }

    public static VisualizerFragment newInstance() {
        VisualizerFragment visualizerFragment = new VisualizerFragment();
        return visualizerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        visualizerLayout = (LinearLayout) inflater.inflate(R.layout.visualizer_fragment, container, false);
        visualizerLayout.setOnClickListener(this);
        initVisualizerViews();

        return visualizerLayout;
    }

    public void initVisualizerViews() {
        visualizers = Utils.getAllMPVisualizerViews(getContext());
        visualizerViewIndex = 0;
        BaseVisualizer baseVisualizer = visualizers.get(visualizerViewIndex);
        baseVisualizer.setPlayer(activity.getAudioSessionIdOfMediaPlayer());
        addReplaceView(baseVisualizer);
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
        visualizerViewIndex = visualizerViewIndex % visualizers.size();

        BaseVisualizer baseVisualizer = visualizers.get(visualizerViewIndex);
        baseVisualizer.setPlayer(activity.getAudioSessionIdOfMediaPlayer());
        addReplaceView(baseVisualizer);
    }

    public void addReplaceView(View view) {
        currentVisualizerView = view;
        visualizerLayout.removeAllViews();
        visualizerLayout.addView(view);
    }

    public View getCurrentVisualizerView() {
        return currentVisualizerView;
    }
}
