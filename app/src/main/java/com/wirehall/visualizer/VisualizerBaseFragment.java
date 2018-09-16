package com.wirehall.visualizer;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.wirehall.audiorecorder.R;

public abstract class VisualizerBaseFragment extends Fragment {


    public interface Media {
        int getAudioSessionIdOfMediaPlayer();
    }

    Media activity;

    public VisualizerBaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Media) context;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        init();
    }

    protected void init() {
        BaseVisualizer baseVisualizer = getView().findViewById(R.id.visualizer);
        baseVisualizer.setColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        baseVisualizer.setPlayer(activity.getAudioSessionIdOfMediaPlayer());
    }

}
