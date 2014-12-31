package com.sibigtroth.botimer;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SynesthetizerFragment extends Fragment {

  private static final String TAG = "SynesthetizerFragment";
  private Random mRandom = new Random();
  private HashMap<View, Synesthetizer.PaletteColor> mPaletteColorButtonToPaletteColor = new HashMap<>();
  private TonePlayer mTonePlayer;

  public SynesthetizerFragment() {
    mTonePlayer = new TonePlayer();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_synesthetizer, container, false);
    ImageView capturedImageView = (ImageView) view.findViewById(R.id.synesthetizerCaputuredImage);
    capturedImageView.setOnTouchListener(capturedImageOnTouchListener);
    return view;
  }

  private LinearLayout getPaletteColorButtonsContainer() {
    return (LinearLayout) getActivity().findViewById(R.id.paletteColorButtonsContainer);
  }

  View.OnClickListener paletteColorButtonOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View paletteColorButton) {
      playPaletteTone(mPaletteColorButtonToPaletteColor.get(paletteColorButton));
    }
  };

  View.OnTouchListener capturedImageOnTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      onCapturedImageTouched(event.getX(), event.getY());
      return false;
    }
  };

  private void onCapturedImageTouched(float x, float y) {
    ImageView imageView = (ImageView) getActivity().findViewById(R.id.synesthetizerCaputuredImage);
    Bitmap bitmap = BitmapFactory.decodeFile(getActivity().getExternalFilesDir(null).getAbsolutePath() + "/synesthetizerCapturedImage.jpg");
  }

  public void setCapturedImage(String imageFilePath) {
    Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
    ImageView imageView = (ImageView) getActivity().findViewById(R.id.synesthetizerCaputuredImage);
    imageView.setImageBitmap(bitmap);
  }

  public void loadPalette(ArrayList<Synesthetizer.PaletteColor> paletteColors) {
    updatePaletteDisplay(paletteColors);
    updatePaletteTones(paletteColors);
    playPaletteTonesInSequence(paletteColors);
  }

  private void updatePaletteDisplay(ArrayList<Synesthetizer.PaletteColor> paletteColors) {
    clearPaletteDisplay();
    for (int i = 0; i < paletteColors.size(); i++) {
      createPaletteColorButton(paletteColors.get(i));
    }
  }

  private void updatePaletteTones(ArrayList<Synesthetizer.PaletteColor> paletteColors) {
    mTonePlayer.clearTones();
    for (int i = 0; i < paletteColors.size(); i++) {
      int frequency = paletteColors.get(i).toneFrequency;
      float duration = .5f;
      mTonePlayer.createTone(frequency, duration);
    }
  }

  private void playPaletteTonesInSequence(ArrayList<Synesthetizer.PaletteColor> paletteColors) {
    for (int i = 0; i < paletteColors.size(); i++) {
      int delay = 600 * i + 1000;
      mTonePlayer.playToneAfterDelay(i, delay);
    }
  }

  private void playPaletteTone(Synesthetizer.PaletteColor paletteColor) {
    mTonePlayer.playTone(paletteColor.clusterIndex);
  }

  private void clearPaletteDisplay() {
    for (View paletteColorButton : mPaletteColorButtonToPaletteColor.keySet()) {
      getPaletteColorButtonsContainer().removeView(paletteColorButton);
    }
    mPaletteColorButtonToPaletteColor = new HashMap<>();
  }

  private void createPaletteColorButton(Synesthetizer.PaletteColor paletteColor) {
    View paletteColorButton = LayoutInflater.from(getActivity()).inflate(R.layout.palette_color_button, null);
    paletteColorButton.setBackgroundColor(paletteColor.color);
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    paletteColorButton.setLayoutParams(layoutParams);
    paletteColorButton.setOnClickListener(paletteColorButtonOnClickListener);
    getPaletteColorButtonsContainer().addView(paletteColorButton);
    mPaletteColorButtonToPaletteColor.put(paletteColorButton, paletteColor);
  }
}
