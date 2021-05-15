package com.wirehall.audiorecorder.recorder;

import android.os.SystemClock;

import androidx.annotation.NonNull;

public class RecordingTime {

  private long totalRecTime = 0L;
  private long recStartTime = 0L;
  private long recPauseTime = 0L;

  public long autoSetTotalRecTime() {
    this.totalRecTime = SystemClock.uptimeMillis() - recStartTime - recPauseTime;
    return this.totalRecTime;
  }

  public void setRecStartTime(long recStartTime) {
    this.recStartTime = recStartTime;
  }

  public void autoSetRecPauseTime() {
    this.recPauseTime = SystemClock.uptimeMillis() - recStartTime - totalRecTime;
  }

  public void reset() {
    totalRecTime = 0;
    recPauseTime = 0;
    recStartTime = 0;
  }

  @NonNull
  @Override
  public String toString() {
    return "RecordingTime{"
        + "totalRecTime="
        + totalRecTime
        + ", recStartTime="
        + recStartTime
        + ", recPauseTime="
        + recPauseTime
        + '}';
  }
}
