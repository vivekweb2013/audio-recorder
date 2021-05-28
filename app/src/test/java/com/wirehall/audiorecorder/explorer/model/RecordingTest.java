package com.wirehall.audiorecorder.explorer.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RecordingTest {
  private static final String PATH = "/test/abc.3gp";

  @Test
  public void compare_isEqual_hashcode() {
    Recording rec1 = new Recording();
    rec1.setName("rec1");
    rec1.setPath(PATH);
    rec1.setDuration(100);
    rec1.setDurationDetailedInString("100 millis");
    rec1.setDurationShortInString("100 m");
    rec1.setModifiedDateMilliSec(11111111111111L);
    rec1.setModifiedDateInString("01-JAN-2020");
    rec1.setSize(1000);
    rec1.setSizeInString("1000 bytes");
    rec1.setPlaying(false);

    Recording rec2 = new Recording();
    rec2.setName(rec1.getName());
    rec2.setPath(rec1.getPath());
    rec2.setDuration(rec1.getDuration());
    rec2.setDurationDetailedInString(rec1.getDurationDetailedInString());
    rec2.setDurationShortInString(rec1.getDurationShortInString());
    rec2.setModifiedDateMilliSec(rec1.getModifiedDateMilliSec());
    rec2.setModifiedDateInString(rec1.getModifiedDateInString());
    rec2.setSize(rec1.getSize());
    rec2.setSizeInString(rec1.getSizeInString());
    rec2.setPlaying(rec1.isPlaying());

    Recording rec3 = new Recording();
    rec3.setName("rec3");
    rec3.setPath(PATH);
    rec3.setDuration(300);
    rec3.setDurationDetailedInString("300 millis");
    rec3.setDurationShortInString("300 m");
    rec3.setModifiedDateMilliSec(3333333333L);
    rec3.setModifiedDateInString("03-JAN-2020");
    rec3.setSize(3000);
    rec3.setSizeInString("3000 bytes");
    rec3.setPlaying(true);

    Recording rec4 = new Recording();
    rec4.setPath(PATH);

    assertEquals(rec1, rec1);
    assertEquals(rec1.hashCode(), rec1.hashCode());

    assertEquals(rec1, rec2);
    assertEquals(rec1.hashCode(), rec2.hashCode());

    assertEquals(rec1, rec3);
    assertEquals(rec1.hashCode(), rec3.hashCode());

    assertEquals(rec1, rec4);
    assertEquals(rec1.hashCode(), rec4.hashCode());

    assertNotEquals(rec1, null);
  }
}
