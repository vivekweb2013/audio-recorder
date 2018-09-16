package com.wirehall.visualizer;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wirehall.audiorecorder.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class VisualizerFragment extends VisualizerBaseFragment {


    public VisualizerFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.visualizer_fragment, container, false);
    }

}
