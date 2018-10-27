package com.wirehall.audiorecorder.mr;

/**
 * Since the MediaRecorder class does not hold the state
 * We need to maintain the state using the instance of this enum
 */
public enum MediaRecorderState {
    RECORDING, PAUSED, STOPPED
}
