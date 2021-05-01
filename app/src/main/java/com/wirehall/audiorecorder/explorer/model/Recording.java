package com.wirehall.audiorecorder.explorer.model;

import androidx.annotation.NonNull;

public class Recording {
    private String name;
    private String path;
    private long size;
    private String sizeInString;
    private long modifiedDateMilliSec;
    private String modifiedDateInString;
    private long duration;
    private String durationDetailedInString;
    private String durationShortInString;
    private boolean isPlaying;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSizeInString() {
        return sizeInString;
    }

    public void setSizeInString(String sizeInString) {
        this.sizeInString = sizeInString;
    }

    public long getModifiedDateMilliSec() {
        return modifiedDateMilliSec;
    }

    public void setModifiedDateMilliSec(long modifiedDateMilliSec) {
        this.modifiedDateMilliSec = modifiedDateMilliSec;
    }

    public String getModifiedDateInString() {
        return modifiedDateInString;
    }

    public void setModifiedDateInString(String modifiedDateInString) {
        this.modifiedDateInString = modifiedDateInString;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDurationDetailedInString() {
        return durationDetailedInString;
    }

    public void setDurationDetailedInString(String durationDetailedInString) {
        this.durationDetailedInString = durationDetailedInString;
    }

    public String getDurationShortInString() {
        return durationShortInString;
    }

    public void setDurationShortInString(String durationShortInString) {
        this.durationShortInString = durationShortInString;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    @NonNull
    @Override
    public String toString() {
        return "Recording{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", sizeInString='" + sizeInString + '\'' +
                ", modifiedDateMilliSec=" + modifiedDateMilliSec +
                ", modifiedDateInString='" + modifiedDateInString + '\'' +
                ", duration=" + duration +
                ", durationDetailedInString='" + durationDetailedInString + '\'' +
                ", durationShortInString='" + durationShortInString + '\'' +
                ", isPlaying=" + isPlaying +
                '}';
    }

    @Override
    public boolean equals(Object otherRecordingObject) {
        return otherRecordingObject != null && otherRecordingObject.getClass() == getClass() && this.path.equals(((Recording) otherRecordingObject).getPath());
    }
}
