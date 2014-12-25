package com.sibigtroth.botimer;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sibigtroth on 12/25/14.
 */
public class Synesthetizer {

  private MainActivity mMainActivity;
  public ArrayList<String> HOT_PHRASES;

  public Synesthetizer(MainActivity mainActivity) {
    mMainActivity = mainActivity;

    initialize();
  }

  private void initialize() {
    createSynesthesiaHotPhrases();
  }

  private void createSynesthesiaHotPhrases() {
    HOT_PHRASES = new ArrayList<>(Arrays.asList(
        "play what you see"
    ));
  }

}
