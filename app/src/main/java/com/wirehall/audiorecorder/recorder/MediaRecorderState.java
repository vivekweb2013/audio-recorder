package com.wirehall.audiorecorder.recorder;

/**
 * Since the MediaRecorder class does not hold the state We need to maintain the state using the
 * instance of this enum
 */
public enum MediaRecorderState {
  RECORDING,
  RESUMED,
  PAUSED,
  STOPPED,
  DISCARDED;

  public boolean isRecording() {
    return this == RECORDING || this == RESUMED;
  }

  public boolean isStopped() {
    return this == STOPPED || this == DISCARDED;
  }
}
