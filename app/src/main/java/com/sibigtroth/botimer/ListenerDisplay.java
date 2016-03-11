package com.sibigtroth.botimer;

import android.view.View;
import android.view.animation.AlphaAnimation;

/**
 * Created by sibigtroth on 12/24/14.
 */
public class ListenerDisplay {

  private MainActivity mMainActivity;
  private View mListeningIndicator;
  private static final int LISTENING_INDICATOR_FADE_DURATION = 1000;

  public ListenerDisplay(MainActivity mainActivity) {
    mMainActivity = mainActivity;

    initialize();
  }

  private void initialize() {
    mListeningIndicator = mMainActivity.findViewById(R.id.listeningIndicator);
    mListeningIndicator.setOnClickListener(mOnListeningIndicatorClick);
    fadeUpListeningIndicator();
  }

  public interface ListenerDisplayCallback {
    public void onListeningIndicatorClicked();
  }

  View.OnClickListener mOnListeningIndicatorClick = new View.OnClickListener() {

    @Override
    public void onClick(View v) {
      fadeUpListeningIndicator();
      mMainActivity.onListeningIndicatorClicked();
    }
  };

  public void fadeUpListeningIndicator() {
    fadeListeningIndicator(.25f, 1f, LISTENING_INDICATOR_FADE_DURATION);
  }

  public void fadeDownListeningIndicator() {
    fadeListeningIndicator(1.0f, .25f, LISTENING_INDICATOR_FADE_DURATION);
  }

  private void fadeListeningIndicator(final float startAlpha, final float endAlpha, final long duration) {
    mMainActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        AlphaAnimation animation = new AlphaAnimation(startAlpha, endAlpha);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        mListeningIndicator.startAnimation(animation);
      }
    });
  }
}
