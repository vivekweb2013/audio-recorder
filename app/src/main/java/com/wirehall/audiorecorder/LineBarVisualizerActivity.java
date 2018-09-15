package com.wirehall.audiorecorder;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;

import com.wirehall.visualizer.BaseVisualizer;
import com.wirehall.visualizer.VisualizerBaseActivity;

public class LineBarVisualizerActivity extends VisualizerBaseActivity {

    @Override
    protected void init() {
        BaseVisualizer lineBarVisualizer = findViewById(R.id.visualizer);
        lineBarVisualizer.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
        lineBarVisualizer.setPlayer(mediaPlayer.getAudioSessionId());
    }

    @Override
    protected int getLayout() {
        return R.layout.main_activity;
    }
}
