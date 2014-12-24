package com.sibigtroth.botimer;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by sibigtroth on 12/23/14.
 */
public class Speaker {

  private MainActivity mMainActivity;
  private TextToSpeech mTextToSpeech;
  private HashMap<String, String> mTtsUtteranceMap;

  public Speaker(MainActivity mainActivity) {
    mMainActivity = mainActivity;

    initialize();
  }

  private void initialize() {
    mTtsUtteranceMap = new HashMap<>();
    mTextToSpeech = new TextToSpeech(mMainActivity, new TtsInitListener());
    mTextToSpeech.setOnUtteranceProgressListener(new TtsUtteranceListener());
  }


  ///////////////////////////
  // callbacks
  ///////////////////////////

  public interface SpeakerCallback {

    public void onTtsSpeakStart();

    public void onTtsSpeakDone();
  }


  ///////////////////////////
  // utilities
  ///////////////////////////

  public void speak(String textToSpeak) {
    Log.d("foo", "Speak:    " + textToSpeak);
    mTtsUtteranceMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
    // TODO: should use these arguments instead: (CharSequence text, int queueMode, Bundle params, String utteranceId)
    mTextToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_ADD, mTtsUtteranceMap);

  }

  /*
  public void speakAfterDelay(final String textToSpeak, int delay)
  {
      Looper.prepare();
      Handler delayHandler= new Handler();
      Runnable r=new Runnable()
      {
          @Override
          public void run()
          {
              speak(textToSpeak);
          }

      };
      delayHandler.postDelayed(r, delay);
  }
  */

  public void shutDown() {
    mTextToSpeech.shutdown();
  }


  class TtsInitListener implements TextToSpeech.OnInitListener {
    public TtsInitListener() {
    }

    @Override
    public void onInit(int status) {
      if (status == TextToSpeech.SUCCESS) {
      } else {
      }
    }
  }


  class TtsUtteranceListener extends UtteranceProgressListener {

    public TtsUtteranceListener() {
    }

    @Override
    public void onDone(String utteranceId) {
      mMainActivity.onTtsSpeakDone();
    }

    @Override
    public void onError(String utteranceId) {
    }

    @Override
    public void onStart(String utteranceId) {
      mMainActivity.onTtsSpeakStart();
    }
  }
}


