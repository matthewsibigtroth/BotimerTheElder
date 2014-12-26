package com.sibigtroth.botimer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

/*
 TODO:
 show ui feedback when hearing voice input (using onRmsChanged callback
 clean up returned thinking response (e.g. "By &quot;we&quot; do you mean you and me?")
 thinking display
*/

public class MainActivity extends Activity implements Speaker.SpeakerCallback,
    Thinker.ThinkerCallback,
    Listener.ListenerCallback,
    Knower.KnowerCallback,
    Recognizer.RecognizerCallback,
    ListenerDisplay.ListenerDisplayCallback {

  private static final String TAG = "MainActivity";
  private Speaker mSpeaker;
  private SpeakerDisplay mSpeakerDisplay;
  private Listener mListener;
  private ListenerDisplay mListenerDisplay;
  private Thinker mThinker;
  private Knower mKnower;
  private Recognizer mRecognizer;
  private Synesthetizer mSynesthetizer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initialize();
  }

  private void initialize() {
    mSpeaker = new Speaker(this);
    mSpeakerDisplay = new SpeakerDisplay(this);

    mThinker = new Thinker(this);

    mKnower = new Knower(this);

    mRecognizer = new Recognizer(this);

    mSynesthetizer = new Synesthetizer(this);

    mListener = new Listener(this);
    mListenerDisplay = new ListenerDisplay(this);
    mListener.listen();
  }


  ////////////////////////////////////////
  // callbacks
  ////////////////////////////////////////

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mListener.shutDown();
    mSpeaker.shutDown();
  }

  @Override
  public void onTtsSpeakStart() {
    mSpeakerDisplay.startAnimatingSpeakingIndicator();
  }

  @Override
  public void onTtsSpeakDone() {
    mListener.listen();
    mListenerDisplay.fadeUpListeningIndicator();
    mSpeakerDisplay.stopAnimatingSpeakingIndicator();
  }

  @Override
  public void onThinkingDone(String speechResponse) {
    mSpeaker.speak(speechResponse);
  }

  @Override
  public void onSpeechRecognized(String recognizedSpeech) {
    Log.d(TAG, "onSpeechRecognized:  " + recognizedSpeech);
    mListenerDisplay.fadeDownListeningIndicator();
    handleRecognizedSpeech(recognizedSpeech);
  }

  @Override
  public void onNoRecognizedSpeechFound() {
    mSpeaker.speak("I'm not sure I understood you");
  }

  @Override
  public void onSpeechRecognitionTimeout() {
    mListenerDisplay.fadeDownListeningIndicator();
  }

  @Override
  public void onFreebaseNodeDataFound(Knower.FreebaseNodeData freebaseNodeData, String inputText) {
    KnowerFragment knowerFragment = (KnowerFragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
    knowerFragment.updateKnowledgeCard(freebaseNodeData, inputText);
  }

  @Override
  public void onRelatedFreebaseNodeDataFound(Knower.FreebaseNodeData FreebaseNodeData, String inputText) {

  }

  @Override
  public void onImageRecognitionComplete(String filePath_image, String recognizedObject) {

  }

  @Override
  public void onListeningIndicatorClicked() {
    mListener.listen();
    //showKnowledgeFragment();
    //mKnower.findFreebaseNodeDataForInputText("science");
  }


  ////////////////////////////////////////
  // utilities
  ////////////////////////////////////////

  private void handleRecognizedSpeech(String recognizedSpeech) {
    String hotPhrase = checkForHotPhrase(recognizedSpeech, mKnower.HOT_PHRASES);
    if (hotPhrase != null) {
      handleKnowledgeHotPhrase(hotPhrase, recognizedSpeech);
      return;
    }

    hotPhrase = checkForHotPhrase(recognizedSpeech, mRecognizer.HOT_PHRASES);
    if (hotPhrase != null) {
      handleObjectRecognitionHotPhrase(hotPhrase, recognizedSpeech);
      return;
    }

    hotPhrase = checkForHotPhrase(recognizedSpeech, mSynesthetizer.HOT_PHRASES);
    if (hotPhrase != null) {
      handleSynesthesiaHotPhrase(hotPhrase, recognizedSpeech);
      return;
    }

    mThinker.sayToBot(recognizedSpeech);
  }

  private String checkForHotPhrase(String recognizedSpeech, ArrayList<String> hotPhrases) {
    for (int i = 0; i < hotPhrases.size(); i++) {
      String hotPhrase = hotPhrases.get(i);
      if (recognizedSpeech.contains(hotPhrase) == true) {
        return hotPhrase;
      }
    }
    return null;
  }

  private void handleKnowledgeHotPhrase(String hotPhrase, String recognizedSpeech) {
    showKnowledgeFragment();
    int hotPhraseIndex = recognizedSpeech.indexOf(hotPhrase);
    int startIndex = hotPhraseIndex + hotPhrase.length();
    int stopIndex = recognizedSpeech.length();
    String contentString = recognizedSpeech.substring(startIndex, stopIndex);
    mKnower.findFreebaseNodeDataForInputText(contentString);
  }

  private void showKnowledgeFragment() {
    Fragment fragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
    if (!(fragment instanceof KnowerFragment)) {
      KnowerFragment knowerFragment = new KnowerFragment();
      FragmentManager fragmentManager = getFragmentManager();
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      fragmentTransaction.setCustomAnimations(R.anim.fade_in_and_slide_up_fragment, R.anim.fade_out_fragment)
          .replace(R.id.fragmentContainer, knowerFragment)
          .commit();
    }
  }

  private void handleObjectRecognitionHotPhrase(String hotPhrase, String recognizedSpeech) {

  }

  private void handleSynesthesiaHotPhrase(String hotPhrase, String recognizedSpeech) {

  }

}