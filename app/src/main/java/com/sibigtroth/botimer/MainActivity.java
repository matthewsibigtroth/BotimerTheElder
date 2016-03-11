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
 replace text Alice with Botimer
 default knower card image when none is present
 help text
 busy indicator
 remove dependencies for interfaces
 animate palette color when playing its associated tone
*/

public class MainActivity extends Activity implements Speaker.SpeakerCallback,
    Thinker.ThinkerCallback,
    Listener.ListenerCallback,
    Knower.KnowerCallback,
    Recognizer.RecognizerCallback,
    ListenerDisplay.ListenerDisplayCallback,
    KnowerFragment.KnowerFragmentCallback,
    Synesthetizer.SynesthetizerCallback {

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
    } else {
      mSpeaker.speak("I'm not sure what that is");
    }
  }

  @Override
  public void onListeningIndicatorClicked() {
    mListener.listen();

    //showKnowledgeFragment();
    //mKnower.findFreebaseNodeDataForInputText("science");

    //showRecognizerFragment();
    //captureObjectRecognitionImage();

    //showSynesthetizerFragment();
    //captureSynesthetizerImage();
  }

  @Override
  public void onKnowerCardSingleTap(View cardView, Knower.FreebaseNodeData freebaseNodeData) {
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

  @Override
  public void onSynesthetizerImagePaletteExtracted(ArrayList<Synesthetizer.PaletteColor> paletteColors) {
    getSynesthetizerFragment().loadPalette(paletteColors);
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
    if (!contentString.equals("")) {
      mKnower.findFreebaseNodeDataForInputText(contentString);
    } else {
      mThinker.sayToBot(recognizedSpeech);
    }
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
    showSynesthetizerFragment();
    captureSynesthetizerImage();
  }

  private void captureSynesthetizerImage() {
    File file = new File(mSynesthetizer.CAPTURED_SYNESTHETIZER_IMAGE_FILE_PATH);
    Uri outputFileUri = Uri.fromFile(file);
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
    startActivityForResult(intent, CAPTURE_SYNESTHETIZER_IMAGE_REQUEST);
  }

  private void captureObjectRecognitionImage() {
    File file = new File(mRecognizer.CAPTURED_OBJECT_RECOGNITION_IMAGE_FILE_PATH);
    Uri outputFileUri = Uri.fromFile(file);
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
    startActivityForResult(intent, CAPTURE_OBJECT_RECOGNITION_IMAGE_REQUEST);
  }

  private void handleCapturedSynesthetizerImage() {
    mSynesthetizer.synesthetizeImage(mSynesthetizer.CAPTURED_SYNESTHETIZER_IMAGE_FILE_PATH);
    getSynesthetizerFragment().setCapturedImage(mSynesthetizer.CAPTURED_SYNESTHETIZER_IMAGE_FILE_PATH);
  }

  private SynesthetizerFragment getSynesthetizerFragment() {
    return (SynesthetizerFragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
  }

  private void handleCapturedObjectRecognitionImage() {
    mRecognizer.recognizeImage(mRecognizer.CAPTURED_OBJECT_RECOGNITION_IMAGE_FILE_PATH);
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

  private void showSynesthetizerFragment() {
    Fragment fragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
    if (!(fragment instanceof SynesthetizerFragment)) {
      SynesthetizerFragment synesthetizerFragment = new SynesthetizerFragment();
      FragmentManager fragmentManager = getFragmentManager();
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      fragmentTransaction.setCustomAnimations(R.anim.fade_in_and_slide_up_fragment, R.anim.fade_out_fragment, R.anim.fade_in_fragment, R.anim.fade_out_fragment)
          .replace(R.id.fragmentContainer, synesthetizerFragment)
          .addToBackStack(null)
          .commit();
    }
  }
}