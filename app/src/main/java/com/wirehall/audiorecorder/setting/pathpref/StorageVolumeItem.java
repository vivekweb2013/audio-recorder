package com.wirehall.audiorecorder.setting.pathpref;

public class StorageVolumeItem extends StorageItem {
  public StorageVolumeItem(String name, String path) {
    super(name, path);
  }

  @Override
  public String getStylishName() {
    return "❍ " + super.getName();
  }
}
