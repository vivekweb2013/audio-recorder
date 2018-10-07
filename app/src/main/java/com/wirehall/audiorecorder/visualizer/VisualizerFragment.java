package com.wirehall.audiorecorder.visualizer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.wirehall.audiorecorder.R;

import java.util.ArrayList;
import java.util.List;

public class VisualizerFragment extends Fragment implements OnClickListener {
    public interface VisualizerMPSession {
        int getAudioSessionIdOfMediaPlayer();
    }

    VisualizerMPSession activity;

    private List<BaseVisualizer> visualizers = new ArrayList<>();
    private int currentVisualizerIndex = 0;

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

        if (visualizers.size() == 0) {
            visualizers = instantiateAllVisualizers();
        }

        LinearLayout visualizerLayout = (LinearLayout) inflater.inflate(R.layout.visualizer_fragment, container, false);
        addVisualizerToParentView(visualizerLayout);
        visualizerLayout.setOnClickListener(this);

        return visualizerLayout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (VisualizerMPSession) context;
    }

    @Override
    public void onClick(View view) {
        addVisualizerToParentView((LinearLayout) view);
    }

    private void addVisualizerToParentView(LinearLayout linearLayout) {
        linearLayout.removeAllViews();
        currentVisualizerIndex = currentVisualizerIndex % visualizers.size();
        visualizers.get(currentVisualizerIndex).setPlayer(activity.getAudioSessionIdOfMediaPlayer());
        linearLayout.addView(visualizers.get(currentVisualizerIndex));
        currentVisualizerIndex++;
    }

    @NonNull
    private List<BaseVisualizer> instantiateAllVisualizers() {
        BarVisualizer barVisualizer = new BarVisualizer(getActivity());
        barVisualizer.setColor(ContextCompat.getColor(getActivity(), R.color.visualizerColor));
        barVisualizer.setDensity(100);

        CircleBarVisualizer circleBarVisualizer = new CircleBarVisualizer(getActivity());
        circleBarVisualizer.setColor(ContextCompat.getColor(getActivity(), R.color.visualizerColor));

        CircleVisualizer circleVisualizer = new CircleVisualizer(getActivity());
        circleVisualizer.setColor(ContextCompat.getColor(getActivity(), R.color.visualizerColor));

        LineBarVisualizer lineBarVisualizer = new LineBarVisualizer(getActivity());
        lineBarVisualizer.setColor(ContextCompat.getColor(getActivity(), R.color.visualizerColor));
        lineBarVisualizer.setDensity(100);

        LineVisualizer lineVisualizer = new LineVisualizer(getActivity());
        lineVisualizer.setColor(ContextCompat.getColor(getActivity(), R.color.visualizerColor));

        BlazingColorVisualizer blazingColorVisualizer = new BlazingColorVisualizer(getActivity());
        blazingColorVisualizer.setColor(ContextCompat.getColor(getActivity(), R.color.visualizerColor));

        visualizers.add(barVisualizer);
        visualizers.add(circleBarVisualizer);
        visualizers.add(circleVisualizer);
        visualizers.add(lineBarVisualizer);
        visualizers.add(lineVisualizer);
        visualizers.add(blazingColorVisualizer);

        return visualizers;
    }
}
