package com.wirehall.audiorecorder.visualizer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.visualizer.view.BarVisualizerView;
import com.wirehall.audiorecorder.visualizer.view.BaseVisualizerView;
import com.wirehall.audiorecorder.visualizer.view.BlazingColorVisualizerView;
import com.wirehall.audiorecorder.visualizer.view.CircleBarVisualizerView;
import com.wirehall.audiorecorder.visualizer.view.CircleVisualizerView;
import com.wirehall.audiorecorder.visualizer.view.LineBarVisualizerView;
import com.wirehall.audiorecorder.visualizer.view.LineVisualizerView;
import com.wirehall.audiorecorder.visualizer.view.RecorderVisualizerView;

import java.util.ArrayList;
import java.util.List;

public final class Utils {

    @NonNull
    public static List<BaseVisualizerView> getAllMPVisualizerViews(Context context) {
        List<BaseVisualizerView> visualizers = new ArrayList<>();

        BarVisualizerView barVisualizerView = new BarVisualizerView(context);
        barVisualizerView.setColor(ContextCompat.getColor(context, R.color.visualizerColor));
        barVisualizerView.setDensity(100);
        visualizers.add(barVisualizerView);

        CircleBarVisualizerView circleBarVisualizerView = new CircleBarVisualizerView(context);
        circleBarVisualizerView.setColor(ContextCompat.getColor(context, R.color.visualizerColor));
        visualizers.add(circleBarVisualizerView);

        CircleVisualizerView circleVisualizerView = new CircleVisualizerView(context);
        circleVisualizerView.setColor(ContextCompat.getColor(context, R.color.visualizerColor));
        visualizers.add(circleVisualizerView);

        LineBarVisualizerView lineBarVisualizerView = new LineBarVisualizerView(context);
        lineBarVisualizerView.setColor(ContextCompat.getColor(context, R.color.visualizerColor));
        lineBarVisualizerView.setDensity(100);
        visualizers.add(lineBarVisualizerView);

        LineVisualizerView lineVisualizerView = new LineVisualizerView(context);
        lineVisualizerView.setColor(ContextCompat.getColor(context, R.color.visualizerColor));
        visualizers.add(lineVisualizerView);

        BlazingColorVisualizerView blazingColorVisualizerView = new BlazingColorVisualizerView(context);
        blazingColorVisualizerView.setColor(ContextCompat.getColor(context, R.color.visualizerColor));
        visualizers.add(blazingColorVisualizerView);

        return visualizers;
    }

    @NonNull
    public static RecorderVisualizerView getRecorderVisualizerView(Context context) {
        return new RecorderVisualizerView(context, null);
    }
}
