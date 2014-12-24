package com.sibigtroth.botimer;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity implements Speaker.SpeakerCallback,
    Thinker.ThinkerCallback,
    Listener.ListenerCallback,
    Knower.KnowerCallback,
    Recognizer.RecognizerCallback {

  private Speaker mSpeaker;
  private Listener mListener;
  private Thinker mThinker;
  private Knower mKnower;
  private Recognizer mRecognizer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initialize();
  }

  private void initialize() {
    mSpeaker = new Speaker(this);
    mListener = new Listener(this);
    mThinker = new Thinker(this);
    mKnower = new Knower(this);
    mRecognizer = new Recognizer(this);
  }

  @Override
  public void onTtsSpeakStart() {

  }

  @Override
  public void onTtsSpeakDone() {

  }

  @Override
  public void onThinkingDone(String speechResponse) {

  }

  @Override
  public void onSpeechRecognized(String recognizedSpeech) {

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
}
