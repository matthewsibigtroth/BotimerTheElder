package com.sibigtroth.botimer;


import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

public class KnowerFragment extends Fragment {

  public KnowerFragment() {

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_knower, container, false);
  }

  public void updateKnowledgeCard(final Knower.FreebaseNodeData freebaseNodeData, final String inputText) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        // Update card title
        TextView titleTextView = (TextView) getActivity().findViewById(R.id.knowerCardTitle);
        titleTextView.setText(freebaseNodeData.name);
        // Update card snippet
        TextView snippetTextView = (TextView) getActivity().findViewById(R.id.knowerCardSnippet);
        snippetTextView.setText(freebaseNodeData.text);
        // Update card image
        new UpdateCardImageTask().execute(freebaseNodeData.url_image);
      }
    });
  }

  private class UpdateCardImageTask extends AsyncTask<String, Integer, Long> {
    private Bitmap mBitmap;

    protected Long doInBackground(String... urls) {
      int count = urls.length;
      long totalSize = 0;
      for (int i = 0; i < count; i++) {
        try {
          URL url = new URL(urls[i]);
          mBitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      return totalSize;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(Long result) {
      ImageView imageView = (ImageView) getActivity().findViewById(R.id.knowerCardImage);
      imageView.setImageBitmap(mBitmap);
    }
  }


}
