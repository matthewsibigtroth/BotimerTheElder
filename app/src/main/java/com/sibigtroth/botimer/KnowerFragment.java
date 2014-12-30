package com.sibigtroth.botimer;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class KnowerFragment extends Fragment {

  private static final String TAG = "KnowerFragment";
  private HashMap<View, Knower.FreebaseNodeData> mCardViewToFreebaseNodeData = new HashMap<>();

  public KnowerFragment() {

  }

  public interface KnowerFragmentCallback {
    public void onKnowerCardSingleTap(View cardView, Knower.FreebaseNodeData freebaseNodeData);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_knower, container, false);
  }

  private void onCardImageSet(View cardView) {
    // Add the card to the fragment
    FrameLayout knowerFragmentContainer = (FrameLayout) getActivity().findViewById(R.id.knowerFragmentContainer);
    knowerFragmentContainer.addView(cardView);

    // Animate the card showing
    showCard(cardView);
  }

  public void onCardSingleTap(View cardView) {
    MainActivity mainActivity = (MainActivity) getActivity();
    mainActivity.onKnowerCardSingleTap(cardView, mCardViewToFreebaseNodeData.get(cardView));
  }

  public void onCardSwipe(View cardView, float velocityX, float velocityY) {
    removeCard(cardView, velocityX, velocityY);
  }

  public void createKnowerCard(final Knower.FreebaseNodeData freebaseNodeData, final String inputText) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        // Create the empty card view
        View cardView = LayoutInflater.from(getActivity()).inflate(R.layout.knower_card, null);
        // Listen for touch events
        cardView.setOnTouchListener(new CardOnTouchListener(getActivity(), cardView));
        // Set the card title
        TextView titleTextView = (TextView) cardView.findViewById(R.id.knowerCardTitle);
        titleTextView.setText(freebaseNodeData.name);
        // Set the card snippet
        TextView snippetTextView = (TextView) cardView.findViewById(R.id.knowerCardSnippet);
        snippetTextView.setText(freebaseNodeData.text);
        // Set the card image
        new SetCardImageTask(freebaseNodeData.url_image, cardView).execute();
        // Add to the hash
        mCardViewToFreebaseNodeData.put(cardView, freebaseNodeData);
      }
    });
  }

  public void createDummyCard() {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        // Create the empty card view
        View cardView = LayoutInflater.from(getActivity()).inflate(R.layout.knower_card, null);
        cardView.setOnTouchListener(new CardOnTouchListener(getActivity(), cardView));
        // Add the card to the fragment
        FrameLayout knowerFragmentContainer = (FrameLayout) getActivity().findViewById(R.id.knowerFragmentContainer);
        knowerFragmentContainer.addView(cardView);
      }
    });
  }

  private void showCard(View cardView) {
    ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(cardView, "alpha", 0, 1);
    alphaAnimation.setDuration(400);
    alphaAnimation.setInterpolator(new DecelerateInterpolator());
    alphaAnimation.start();
    ObjectAnimator translateYAnimation = ObjectAnimator.ofFloat(cardView, "y", 350, 0);
    translateYAnimation.setDuration(400);
    translateYAnimation.setInterpolator(new DecelerateInterpolator());
    translateYAnimation.start();
  }

  private void removeCard(View cardView, float velocityX, float velocityY) {
    float startX = cardView.getX();
    float stopX = startX + velocityX / 3;
    ObjectAnimator xAnimator = ObjectAnimator.ofFloat(cardView, "x", startX, stopX);
    xAnimator.setDuration(400);
    xAnimator.setInterpolator(new DecelerateInterpolator());
    float startY = cardView.getY();
    float stopY = startY + velocityY / 3;
    ObjectAnimator yAnimator = ObjectAnimator.ofFloat(cardView, "y", startY, stopY);
    yAnimator.setDuration(400);
    yAnimator.setInterpolator(new DecelerateInterpolator());
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(xAnimator, yAnimator);
    animatorSet.addListener(new HideCardAnimationListener(cardView));
    animatorSet.start();
  }

  public void onRemoveCardAnimationEnd(View cardView) {
    FrameLayout knowerFragmentContainer = (FrameLayout) getActivity().findViewById(R.id.knowerFragmentContainer);
    knowerFragmentContainer.removeView(cardView);
    mCardViewToFreebaseNodeData.remove(cardView);
  }

  private class HideCardAnimationListener implements Animator.AnimatorListener {

    private View mCardView;

    public HideCardAnimationListener(View cardView) {
      mCardView = cardView;
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
      onRemoveCardAnimationEnd(mCardView);
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }
  }


  private class CardOnTouchListener implements View.OnTouchListener {

    private final GestureDetector mGestureDetector;

    public CardOnTouchListener(Context context, View cardView) {
      mGestureDetector = new GestureDetector(context, new GestureListener(cardView));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
      return mGestureDetector.onTouchEvent(event);
    }
  }

  private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_THRESHOLD = 30;
    private static final int SWIPE_VELOCITY_THRESHOLD = 30;
    private View mCardView;

    public GestureListener(View cardView) {
      mCardView = cardView;
    }

    @Override
    public boolean onDown(MotionEvent e) {
      return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      onCardSingleTap(mCardView);
      return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      boolean result = false;
      try {
        float diffY = e2.getY() - e1.getY();
        float diffX = e2.getX() - e1.getX();
        if (Math.abs(diffX) > Math.abs(diffY)) {
          if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            KnowerFragment.this.onCardSwipe(mCardView, velocityX, velocityY);
          }
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
      return result;
    }
  }

  private class SetCardImageTask extends AsyncTask<Void, Void, Boolean> {
    private String mUrl;
    private View mCardView;
    private Bitmap mBitmap;

    public SetCardImageTask(String url, View cardView) {
      mUrl = url;
      mCardView = cardView;
    }

    protected Boolean doInBackground(Void... params) {
      try {
        if (URLUtil.isNetworkUrl(mUrl)) {
          URL url = new URL(mUrl);
          mBitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      return true;
    }

    protected void onPostExecute(Boolean result) {
      ImageView imageView = (ImageView) mCardView.findViewById(R.id.knowerCardImage);
      imageView.setImageBitmap(mBitmap);
      KnowerFragment.this.onCardImageSet(mCardView);
    }
  }
}
