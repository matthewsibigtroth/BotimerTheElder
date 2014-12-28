package com.sibigtroth.botimer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

/*
 TODO:
 show ui feedback when hearing voice input (using onRmsChanged callback
 clean up returned thinking response (e.g. "By &quot;we&quot; do you mean you and me?")
 thinking display
 have a more action button on knower cards which when pressed will speak more of the freebase info
*/

public class MainActivity extends Activity implements Speaker.SpeakerCallback,
    Thinker.ThinkerCallback,
    Listener.ListenerCallback,
    Knower.KnowerCallback,
    Recognizer.RecognizerCallback,
    ListenerDisplay.ListenerDisplayCallback,
    KnowerFragment.KnowerFragmentCallback {

  private static final String TAG = "MainActivity";
  private Speaker mSpeaker;
  private SpeakerDisplay mSpeakerDisplay;
  private Listener mListener;
  private ListenerDisplay mListenerDisplay;
  private Thinker mThinker;
  private Knower mKnower;
  private Recognizer mRecognizer;
  private Synesthetizer mSynesthetizer;
  private static final int CAPTURE_SYNESTHETIZER_IMAGE_REQUEST = 1;
  private static final int CAPTURE_OBJECT_RECOGNITION_IMAGE_REQUEST = 2;
  private String CAPTURED_OBJECT_RECOGNITION_IMAGE_FILE_PATH;
  private String CAPTURED_SYNESTHETIZER_IMAGE_FILE_PATH;

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
    CAPTURED_OBJECT_RECOGNITION_IMAGE_FILE_PATH = this.getExternalFilesDir(null).getAbsolutePath() + "/objectRecognitionCapturedImage.jpg";

    mSynesthetizer = new Synesthetizer(this);
    CAPTURED_SYNESTHETIZER_IMAGE_FILE_PATH = this.getExternalFilesDir(null).getAbsolutePath() + "/synesthetizerCapturedImage.jpg";

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
    knowerFragment.createKnowerCard(freebaseNodeData, inputText);
    mSpeaker.speak(freebaseNodeData.text.split("\\.")[0]);
  }

  @Override
  public void onRelatedFreebaseNodeDataFound(Knower.FreebaseNodeData freebaseNodeData, String inputText) {
    KnowerFragment knowerFragment = (KnowerFragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
    knowerFragment.createKnowerCard(freebaseNodeData, inputText);
    mSpeaker.speak(freebaseNodeData.text.split("\\.")[0]);
  }

  @Override
  public void onImageRecognitionComplete(String imageFilePath, String recognizedObject) {
    if (recognizedObject != null) {
      RecognizerFragment recognizerFragment = (RecognizerFragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
      recognizerFragment.createRecognizerCard(imageFilePath, recognizedObject);
      mSpeaker.speak("This looks like a " + recognizedObject);
    }
    else {
      mSpeaker.speak("I'm not sure what that is");
    }
  }

  @Override
  public void onListeningIndicatorClicked() {
    mListener.listen();

    //showKnowledgeFragment();
    //mKnower.findFreebaseNodeDataForInputText("science");

    //captureSynesthetizerImage();

    //showRecognizerFragment();
    //captureObjectRecognitionImage();
    //String imageFilePath = "/storage/emulated/0/Android/data/com.sibigtroth.botimer/files/objectRecognitionCapturedImage.jpg";
    //String recognizedObject = "purple and black macbook";
    //onImageRecognitionComplete(imageFilePath, recognizedObject);
  }

  @Override
  public void onKnowerCardClicked(View cardView, Knower.FreebaseNodeData freebaseNodeData) {
    mKnower.findRelatedFreebaseNodeDataForInputText(freebaseNodeData.name);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CAPTURE_SYNESTHETIZER_IMAGE_REQUEST) {
      if (resultCode == RESULT_OK) {
        handleCapturedSynesthetizerImage();
      } else if (resultCode == RESULT_CANCELED) {
      } else {
        Log.d(TAG, "camera capture failure");
      }
    } else if (requestCode == CAPTURE_OBJECT_RECOGNITION_IMAGE_REQUEST) {
      if (resultCode == RESULT_OK) {
        handleCapturedObjectRecognitionImage();
      } else if (resultCode == RESULT_CANCELED) {
      } else {
        Log.d(TAG, "camera capture failure");
      }
    }
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
    showRecognizerFragment();
    captureObjectRecognitionImage();
  }

  private void handleSynesthesiaHotPhrase(String hotPhrase, String recognizedSpeech) {
  }

  private void captureSynesthetizerImage() {
    File file = new File(CAPTURED_SYNESTHETIZER_IMAGE_FILE_PATH);
    Uri outputFileUri = Uri.fromFile(file);
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
    startActivityForResult(intent, CAPTURE_SYNESTHETIZER_IMAGE_REQUEST);
  }

  private void captureObjectRecognitionImage() {
    File file = new File(CAPTURED_OBJECT_RECOGNITION_IMAGE_FILE_PATH);
    Uri outputFileUri = Uri.fromFile(file);
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
    startActivityForResult(intent, CAPTURE_OBJECT_RECOGNITION_IMAGE_REQUEST);
  }

  private void handleCapturedSynesthetizerImage() {

  }

  private void handleCapturedObjectRecognitionImage() {
    mRecognizer.recognizeImage(CAPTURED_OBJECT_RECOGNITION_IMAGE_FILE_PATH);
  }

  private void showRecognizerFragment() {
    Fragment fragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
    if (!(fragment instanceof RecognizerFragment)) {
      RecognizerFragment recognizerFragment = new RecognizerFragment();
      FragmentManager fragmentManager = getFragmentManager();
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      fragmentTransaction.setCustomAnimations(R.anim.fade_in_and_slide_up_fragment, R.anim.fade_out_fragment)
          .replace(R.id.fragmentContainer, recognizerFragment)
          .commit();
    }
  }


}