package com.sibigtroth.botimer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements Speaker.SpeakerCallback,
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
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
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
