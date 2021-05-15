package com.wirehall.audiorecorder.setting.pathpref;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Objects;

public class StorageItem {
  public static final String PARENT_DIR_NAME = "..";
  private String name;
  private String path;

  public StorageItem(String name, String path) {
    this.name = name;
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStylishName() {
    return getName().equals(PARENT_DIR_NAME) ? getName() : "❐ " + getName();
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getParent() {
    return getPath() != null ? new File(getPath()).getParent() : null;
  }

  public StorageItem getParentStorageItem() {
    if (getPath() == null) {
      return null;
    }

    File parentFile = new File(getPath()).getParentFile();
    return new StorageItem(Objects.requireNonNull(parentFile).getName(), parentFile.getPath());
  }

  @NonNull
  @Override
  public String toString() {
    return "StorageItem{" + "name='" + name + '\'' + ", path='" + path + '\'' + '}';
  }
}
