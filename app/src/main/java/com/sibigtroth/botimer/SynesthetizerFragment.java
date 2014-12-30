package com.sibigtroth.botimer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class SynesthetizerFragment extends Fragment {

  public SynesthetizerFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_synesthetizer, container, false);
  }

  public void setCapturedImage(String imageFilePath) {
    Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
    ImageView imageView = (ImageView) getActivity().findViewById(R.id.synesthetizerCaputuredImage);
    imageView.setImageBitmap(bitmap);
  }

}
