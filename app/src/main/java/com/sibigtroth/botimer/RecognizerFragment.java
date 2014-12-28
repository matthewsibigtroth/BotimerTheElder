package com.sibigtroth.botimer;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

public class RecognizerFragment extends Fragment {

  private HashMap<View, String> mCardViewToRecognizedObject = new HashMap<>();

  public RecognizerFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_recognizer, container, false);
  }

  public void createRecognizerCard(final String imageFilePath, final String recognizedObject) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        // Create the empty card view
        View cardView = LayoutInflater.from(getActivity()).inflate(R.layout.recognizer_card, null);
        // Set the card title
        TextView titleTextView = (TextView) cardView.findViewById(R.id.recognizerCardTitle);
        titleTextView.setText(recognizedObject);
        // Set the card snippet
        //TextView snippetTextView = (TextView) cardView.findViewById(R.id.recognizerCardSnippet);
        //snippetTextView.setText(imageFilePath);
        // Set the card image
        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
        ImageView imageView = (ImageView) cardView.findViewById(R.id.recognizerCardImage);
        imageView.setImageBitmap(bitmap);
        // Add to the hash map
        mCardViewToRecognizedObject.put(cardView, recognizedObject);
        onCardCreated(cardView);
      }
    });
  }

  private void onCardCreated(View cardView) {
    // Add the card to the fragment
    FrameLayout knowerFragmentContainer = (FrameLayout) getActivity().findViewById(R.id.recognizerFragmentContainer);
    knowerFragmentContainer.addView(cardView);

    // Animate the card
    ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(cardView, "alpha", 0, 1);
    alphaAnimation.setDuration(400);
    alphaAnimation.setInterpolator(new DecelerateInterpolator());
    alphaAnimation.start();
    ObjectAnimator translateYAnimation = ObjectAnimator.ofFloat(cardView, "y", 350, 0);
    translateYAnimation.setDuration(400);
    translateYAnimation.setInterpolator(new DecelerateInterpolator());
    translateYAnimation.start();
  }



}
