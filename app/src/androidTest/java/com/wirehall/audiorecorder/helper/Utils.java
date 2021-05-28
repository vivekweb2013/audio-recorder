package com.wirehall.audiorecorder.helper;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import org.hamcrest.Matcher;

import java.util.Random;

import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

public class Utils {

  public static final int WAIT_3_SEC = 3000;

  /** Perform action of waiting for a specific time. */
  public static ViewAction waitFor(final long millis) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isRoot();
      }

      @Override
      public String getDescription() {
        return "Wait for " + millis + " milliseconds.";
      }

      @Override
      public void perform(UiController uiController, final View view) {
        uiController.loopMainThreadForAtLeast(millis);
      }
    };
  }

  public static String generateRandomString() {
    int leftLimit = 48; // numeral '0'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = 10;
    Random random = new Random();

    return random
        .ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(targetStringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }
}
