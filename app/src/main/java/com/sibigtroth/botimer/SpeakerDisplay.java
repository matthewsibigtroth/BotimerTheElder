package com.sibigtroth.botimer;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.util.Random;

/**
 * Created by sibigtroth on 12/24/14.
 */
public class SpeakerDisplay {

  private final static String TAG = "SpeakerDisplay";
  private MainActivity mMainActivity;
  private boolean mShouldAnimateSpeakingIndicator;
  private View mSpeakingIndicator;
  private static final int SPEAKING_INDICATOR_FADE_DURATION_MIN = 100;
  private static final int SPEAKING_INDICATOR_FADE_DURATION_MAX = 250;
  private float mCurrentAlpha = 1;

  public SpeakerDisplay(MainActivity mainActivity) {
    mMainActivity = mainActivity;

    initialize();
  }

  private void initialize() {
    mSpeakingIndicator = mMainActivity.findViewById(R.id.speakingIndicator);
    mShouldAnimateSpeakingIndicator = false;
  }

  public void startAnimatingSpeakingIndicator() {
    mShouldAnimateSpeakingIndicator = true;
    fadeSpeakingIndicatorToRandomValue();
  }

  public void stopAnimatingSpeakingIndicator() {
    mShouldAnimateSpeakingIndicator = false;
  }

  private void fadeSpeakingIndicatorToRandomValue() {
    mMainActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        float endAlpha = new Random().nextFloat();
        long duration = new Random().nextInt(SPEAKING_INDICATOR_FADE_DURATION_MAX - SPEAKING_INDICATOR_FADE_DURATION_MIN) + SPEAKING_INDICATOR_FADE_DURATION_MIN;
        AlphaAnimation animation = new AlphaAnimation(mCurrentAlpha, endAlpha);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        animation.setAnimationListener(mFadeSpeakingIndicatorAnimationListener);
        mSpeakingIndicator.startAnimation(animation);
        mCurrentAlpha = endAlpha;
      }
    });
  }

  Animation.AnimationListener mFadeSpeakingIndicatorAnimationListener = new Animation.AnimationListener() {
    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
      if (mShouldAnimateSpeakingIndicator) {
        fadeSpeakingIndicatorToRandomValue();
      }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
  };
}
