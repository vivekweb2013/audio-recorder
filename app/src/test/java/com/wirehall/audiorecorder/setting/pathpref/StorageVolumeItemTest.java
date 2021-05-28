package com.wirehall.audiorecorder.setting.pathpref;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StorageVolumeItemTest {
  @Test
  public void checkPrefix() {
    StorageVolumeItem storageVolumeItem = new StorageVolumeItem("vol1", "/home/vol1");
    String name = storageVolumeItem.getStylishName();
    assertEquals(name, StorageVolumeItem.PREFIX + storageVolumeItem.getName());
  }
}
