package com.wirehall.audiorecorder.setting.pathpref;

public class StorageVolumeItem extends StorageItem {
  public static final String PREFIX = "❍ ";

  public StorageVolumeItem(String name, String path) {
    super(name, path);
  }

  @Override
  public String getStylishName() {
    return PREFIX + super.getName();
  }
}
