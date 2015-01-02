package com.sibigtroth.botimer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
  private ArrayList<Synesthetizer.PaletteColor> mPaletteColors;

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

  private void onCapturedImageTouched(float imageViewX, float imageViewY) {
    Log.d(TAG, "conCapturedImageTouched");

    ImageView imageView = (ImageView) getActivity().findViewById(R.id.synesthetizerCaputuredImage);
    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
    // Map the imageView coords to the correlate bitmap coords
    float scale = (float) imageView.getWidth() / (float) bitmap.getWidth();
    int bitmapX = (int) (imageViewX / scale);
    int bitmapY = (int) (imageViewY / scale);
    // Get the color of the bitmap pixel
    int pixel = bitmap.getPixel(bitmapX, bitmapY);
    // Create the pixel display
    createPixelDisplay(pixel, imageViewX, imageViewY);
    // Play the tone for this pixel's palette color
    Synesthetizer.PaletteColor nearestPaletteColor = findPaletteColorNearestToPixel(pixel);
    playPaletteTone(nearestPaletteColor);
  }

  public Synesthetizer.PaletteColor findPaletteColorNearestToPixel(int pixel) {
    Synesthetizer.PaletteColor nearestPaletteColor = null;
    double minDistance = Integer.MAX_VALUE;
    Synesthetizer.Point pixelPoint = new Synesthetizer.Point(Color.red(pixel), Color.green(pixel), Color.blue(pixel), 0, 0);
    for (Synesthetizer.PaletteColor paletteColor : mPaletteColors) {
      Synesthetizer.Point palettePoint = new Synesthetizer.Point(Color.red(paletteColor.color), Color.green(paletteColor.color), Color.blue(paletteColor.color), 0, 0);
      double distance = computeDistance(pixelPoint, palettePoint);
      if (distance < minDistance) {
        minDistance = distance;
        nearestPaletteColor = paletteColor;
      }
    }
    return nearestPaletteColor;
  }

  // Compute the Cartesian distance between two points.
  private double computeDistance(Synesthetizer.Point a, Synesthetizer.Point b) {
    return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) + (a.z - b.z) * (a.z - b.z));
  }


  private class PixelDisplayAnimatorListener implements Animator.AnimatorListener {

    private View mPixelDisplay;

    public PixelDisplayAnimatorListener(View pixelDisplay) {
      mPixelDisplay = pixelDisplay;
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
      FrameLayout capturedImageContainer = (FrameLayout) getActivity().findViewById(R.id.capturedImageContainer);
      capturedImageContainer.removeView(mPixelDisplay);
    }

    ;

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }
  }

  private void createPixelDisplay(int pixel, float imageViewX, float imageViewY) {
    int pixelSize = 200;
    int strokeSize = 5;
    PixelDisplay pixelDisplay = new PixelDisplay(getActivity(), Color.argb(255, Color.red(pixel), Color.green(pixel), Color.blue(pixel)), pixelSize, pixelSize, strokeSize);
    FrameLayout capturedImageContainer = (FrameLayout) getActivity().findViewById(R.id.capturedImageContainer);
    capturedImageContainer.addView(pixelDisplay);
    pixelDisplay.getLayoutParams().width = pixelSize;
    pixelDisplay.getLayoutParams().height = pixelSize;
    pixelDisplay.setX(imageViewX - pixelSize / 2f);
    pixelDisplay.setY(imageViewY - pixelSize / 2f);
    //pixelDisplay.invalidate();
    //capturedImageContainer.invalidate();
    // Animate the pixel display
    ObjectAnimator scaleXObjectAnimator = ObjectAnimator.ofFloat(pixelDisplay, "scaleX", 0, 1).setDuration(250);
    ObjectAnimator scaleYObjectAnimator = ObjectAnimator.ofFloat(pixelDisplay, "scaleY", 0, 1).setDuration(250);
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(scaleXObjectAnimator, scaleYObjectAnimator);
    animatorSet.start();
    ObjectAnimator alphaObjectAnimator = ObjectAnimator.ofFloat(pixelDisplay, "alpha", 1, 0).setDuration(1200);
    alphaObjectAnimator.setStartDelay(250);
    alphaObjectAnimator.addListener(new PixelDisplayAnimatorListener(pixelDisplay));
    alphaObjectAnimator.start();
  }

  public void setCapturedImage(String imageFilePath) {
    Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
    ImageView imageView = (ImageView) getActivity().findViewById(R.id.synesthetizerCaputuredImage);
    imageView.setImageBitmap(bitmap);
  }

  public void loadPalette(ArrayList<Synesthetizer.PaletteColor> paletteColors) {
    mPaletteColors = paletteColors;
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

  class PixelDisplay extends View {

    public int mColor;
    public int mWidth;
    public int mHeight;
    public int mStrokeSize;
    private Paint mPaint = new Paint();

    public PixelDisplay(Context context, int color, int width, int height, int strokeSize) {
      super(context);

      mColor = color;
      mWidth = width;
      mHeight = height;
      mStrokeSize = strokeSize;
    }

    @Override
    public void onDraw(Canvas canvas) {
      mPaint.setAlpha(1);
      mPaint.setColor(mColor);
      mPaint.setStrokeWidth(0);
      mPaint.setStyle(Paint.Style.FILL);
      canvas.drawCircle(mWidth / 2f, mHeight / 2f, mWidth / 2f, mPaint);
    }
  }
}
