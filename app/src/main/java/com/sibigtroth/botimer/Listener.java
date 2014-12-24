package com.sibigtroth.botimer;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by sibigtroth on 12/23/14.
 */
public class Listener {

  private MainActivity mMainActivity;
  public SpeechRecognizer mSpeechRecognizer;

  public Listener(MainActivity mainActivity) {
    mMainActivity = mainActivity;
    mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mMainActivity);
    mSpeechRecognizer.setRecognitionListener(new ExtendedRecognitionListener());
  }


  ///////////////////////////
  //callbacks
  ///////////////////////////

  public interface ListenerCallback {
    public void onSpeechRecognized(String recognizedSpeech);

    public void onNoRecognizedSpeechFound();
  }


  ///////////////////////////
  //utilities
  ///////////////////////////

  public void listen() {
    mMainActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        mSpeechRecognizer.startListening(intent);
      }
    });
  }

  public void stopListening() {
    mMainActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mSpeechRecognizer.stopListening();
      }
    });
  }

  public void shutDown() {
    mSpeechRecognizer.destroy();
  }


  class ExtendedRecognitionListener implements RecognitionListener {

    public ExtendedRecognitionListener() {
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    public void onError(int error) {
      Log.d("foo", "ExtendedRecognitionListener onError: " + String.valueOf(error));
      if (error == SpeechRecognizer.ERROR_NO_MATCH) {
        mMainActivity.onNoRecognizedSpeechFound();
      }
    }

    public void onResults(Bundle results) {
      String str = new String();
      ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      for (int i = 0; i < data.size(); i++) {
        str += data.get(i).toString();
        mMainActivity.onSpeechRecognized(str);
        return;
      }
    }

    public void onPartialResults(Bundle partialResults) {
    }

    public void onEvent(int eventType, Bundle params) {
    }
  }
}



