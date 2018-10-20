package com.wirehall.audiorecorder.visualizer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.wirehall.audiorecorder.R;

import java.util.ArrayList;
import java.util.List;

public final class Utils {

    @NonNull
    public static List<BaseVisualizer> getAllMPVisualizerViews(Context context) {
        List<BaseVisualizer> visualizers = new ArrayList<>();

        BarVisualizer barVisualizer = new BarVisualizer(context);
        barVisualizer.setColor(ContextCompat.getColor(context, R.color.visualizerColor));
        barVisualizer.setDensity(100);
        visualizers.add(barVisualizer);

        CircleBarVisualizer circleBarVisualizer = new CircleBarVisualizer(context);
        circleBarVisualizer.setColor(ContextCompat.getColor(context, R.color.visualizerColor));
        visualizers.add(circleBarVisualizer);

        CircleVisualizer circleVisualizer = new CircleVisualizer(context);
        circleVisualizer.setColor(ContextCompat.getColor(context, R.color.visualizerColor));
        visualizers.add(circleVisualizer);

        LineBarVisualizer lineBarVisualizer = new LineBarVisualizer(context);
        lineBarVisualizer.setColor(ContextCompat.getColor(context, R.color.visualizerColor));
        lineBarVisualizer.setDensity(100);
        visualizers.add(lineBarVisualizer);

        LineVisualizer lineVisualizer = new LineVisualizer(context);
        lineVisualizer.setColor(ContextCompat.getColor(context, R.color.visualizerColor));
        visualizers.add(lineVisualizer);

        BlazingColorVisualizer blazingColorVisualizer = new BlazingColorVisualizer(context);
        blazingColorVisualizer.setColor(ContextCompat.getColor(context, R.color.visualizerColor));
        visualizers.add(blazingColorVisualizer);

        return visualizers;
    }

    public static RecorderVisualizerView getRecorderVisualizerView(Context context) {
        return new RecorderVisualizerView(context, null);
    }
}
