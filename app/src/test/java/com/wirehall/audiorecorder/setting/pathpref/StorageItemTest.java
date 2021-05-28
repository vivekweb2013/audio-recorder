package com.wirehall.audiorecorder.setting.pathpref;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StorageItemTest {
  @Test
  public void testNameString() {
    StorageItem storageItem = new StorageItem("test", "/abc/test");
    assertEquals("test", storageItem.getName());
    assertEquals("/abc/test", storageItem.getPath());
    assertEquals(StorageItem.CHILD_DIR_PREFIX + "test", storageItem.getStylishName());

    // Override the constructor set params
    storageItem.setName("test2");
    storageItem.setPath("/abc/test2");

    assertEquals("test2", storageItem.getName());
    assertEquals("/abc/test2", storageItem.getPath());
    assertEquals(StorageItem.CHILD_DIR_PREFIX + "test2", storageItem.getStylishName());

    // Set name to parent dir syntax
    storageItem.setName("..");
    assertEquals("..", storageItem.getName());
    assertEquals("..", storageItem.getStylishName());
  }
}
