package com.sibigtroth.botimer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initialize();
  }

  private void initialize() {
    mSpeaker = new Speaker(this);
    mThinker = new Thinker(this);
    //mKnower = new Knower(this);
    //mRecognizer = new Recognizer(this);
    mListener = new Listener(this);
    mListener.listen();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mListener.shutDown();
    mSpeaker.shutDown();
  }

  @Override
  public void onTtsSpeakStart() {

  }

  @Override
  public void onTtsSpeakDone() {
    mListener.listen();
  }

  @Override
  public void onThinkingDone(String speechResponse) {
    mSpeaker.speak(speechResponse);
  }

  @Override
  public void onSpeechRecognized(String recognizedSpeech) {
    Log.d(TAG, "onSpeechRecognized:  " + recognizedSpeech);
    mThinker.sayToBot(recognizedSpeech);
  }

  @Override
  public void onNoRecognizedSpeechFound() {
    mSpeaker.speak("I'm not sure I understood you");
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
