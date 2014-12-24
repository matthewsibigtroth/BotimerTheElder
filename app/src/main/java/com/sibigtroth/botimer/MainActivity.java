package com.sibigtroth.botimer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Random;

/*
 TODO:
 show ui feedback when hearing voice input (using onRmsChanged callback
 clean up returned thinking response (e.g. By &quot;we&quot; do you mean you and me?)
*/

public class MainActivity extends Activity implements Speaker.SpeakerCallback,
    Thinker.ThinkerCallback,
    Listener.ListenerCallback,
    Knower.KnowerCallback,
    Recognizer.RecognizerCallback {

  private static final String TAG = "MainActivity";
  private Speaker mSpeaker;
  private Listener mListener;
  private Thinker mThinker;
  private Knower mKnower;
  private Recognizer mRecognizer;
  private View mListeningIndicator;
  private boolean mShouldAnimateSpeakingIndicator;
  private View mSpeakingIndicator;
  private static final int LISTENING_INDICATOR_FADE_DURATION = 1000;
  private static final int SPEAKING_INDICATOR_FADE_DURATION_MIN = 250;
  private static final int SPEAKING_INDICATOR_FADE_DURATION_MAX = 400;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initialize();
  }

  private void initialize() {
    mSpeaker = new Speaker(this);
    mSpeakingIndicator = findViewById(R.id.speakingIndicator);
    mShouldAnimateSpeakingIndicator = false;

    mThinker = new Thinker(this);
    //mKnower = new Knower(this);
    //mRecognizer = new Recognizer(this);

    mListener = new Listener(this);
    mListeningIndicator = findViewById(R.id.listeningIndicator);
    mListeningIndicator.setOnClickListener(mOnListeningIndicatorClick);
    fadeView(mListeningIndicator, 1f, LISTENING_INDICATOR_FADE_DURATION, null);
    mListener.listen();
  }

  View.OnClickListener mOnListeningIndicatorClick = new View.OnClickListener() {

    @Override
    public void onClick(View v) {
      fadeView(mListeningIndicator, 1f, LISTENING_INDICATOR_FADE_DURATION, null);
      mListener.listen();
    }
  };

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mListener.shutDown();
    mSpeaker.shutDown();
  }

  @Override
  public void onTtsSpeakStart() {
    mShouldAnimateSpeakingIndicator = true;
    float newAlpha = new Random().nextFloat();
    long duration = new Random().nextInt(SPEAKING_INDICATOR_FADE_DURATION_MAX);
    fadeView(mSpeakingIndicator, new Random().nextFloat(), duration, mFadeSpeakingIndicatorAnimationListener);
  }

  @Override
  public void onTtsSpeakDone() {
    mListener.listen();
    fadeView(mListeningIndicator, 1f, LISTENING_INDICATOR_FADE_DURATION, null);
    mShouldAnimateSpeakingIndicator = false;
  }

  @Override
  public void onThinkingDone(String speechResponse) {
    mSpeaker.speak(speechResponse);
  }

  @Override
  public void onSpeechRecognized(String recognizedSpeech) {
    fadeView(mSpeakingIndicator, .25f, LISTENING_INDICATOR_FADE_DURATION, null);
    Log.d(TAG, "onSpeechRecognized:  " + recognizedSpeech);
    mThinker.sayToBot(recognizedSpeech);
  }

  @Override
  public void onNoRecognizedSpeechFound() {
    mSpeaker.speak("I'm not sure I understood you");
  }

  @Override
  public void onSpeechRecognitionTimeout() {
    fadeView(mSpeakingIndicator, .25f, LISTENING_INDICATOR_FADE_DURATION, null);
  }

  @Override
  public void onFreebaseNodeDataFound(Knower.FreebaseNodeData FreebaseNodeData, String inputText) {

  }

  @Override
  public void onRelatedFreebaseNodeDataFound(Knower.FreebaseNodeData FreebaseNodeData, String inputText) {

  }

  @Override
  public void onImageRecognitionComplete(String filePath_image, String recognizedObject) {

  }

  private void fadeView(final View view, final float newAlpha, final long duration, final Animation.AnimationListener animationListener) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        float currentAlpha = view.getAlpha();
        AlphaAnimation animation = new AlphaAnimation(currentAlpha, newAlpha);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        if (animationListener != null) {
          animation.setAnimationListener(animationListener);
        }
        view.startAnimation(animation);
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
        float newAlpha = new Random().nextFloat();
        long duration = new Random().nextInt(SPEAKING_INDICATOR_FADE_DURATION_MAX - SPEAKING_INDICATOR_FADE_DURATION_MIN) + SPEAKING_INDICATOR_FADE_DURATION_MIN;
        fadeView(mSpeakingIndicator, new Random().nextFloat(), duration, mFadeSpeakingIndicatorAnimationListener);
      }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
  };
}
