package com.sibigtroth.botimer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sibigtroth on 12/23/14.
 */
public class Recognizer {

  private static final String TAG = "Recognizer";
  private MainActivity mMainActivity;
  public ArrayList<String> HOT_PHRASES;
  private static final String CAMFIND_API_KEY = "q5QVimyNMzOHw6VEbGOdxjO7bYfCMAOZ";
  private static final int SERVER_SIDE_RECOGNITION_PROCESSING_TIME = 10000;
  private static final String CAMFIND_POST_IMAGE_FILE_NAME = "camFindImageToPost.jpg";
  public String CAPTURED_OBJECT_RECOGNITION_IMAGE_FILE_PATH;

  public Recognizer(MainActivity mainActivity) {
    mMainActivity = mainActivity;
    createObjectRecognitionHotPhrases();
    CAPTURED_OBJECT_RECOGNITION_IMAGE_FILE_PATH = mMainActivity.getExternalFilesDir(null).getAbsolutePath() + "/objectRecognitionCapturedImage.jpg";
  }

  private void createObjectRecognitionHotPhrases() {
    HOT_PHRASES = new ArrayList<>(Arrays.asList(
        "what do you see"
    ));
  }


  /////////////////////////////////////
  //callbacks
  /////////////////////////////////////

  public interface RecognizerCallback {
    public void onImageRecognitionComplete(String imageFilePath, String recognizedObject);
  }

  private void onResizedImagePostedToCamFind(final String originalImageFilePath, final String requestToken) {
    mMainActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        askCamFindToRecognizeImage(originalImageFilePath, requestToken);
      }
    });
  }


  /////////////////////////////////////
  //utilities
  /////////////////////////////////////

  public void recognizeImage(final String imageFilePath) {
    Log.d(TAG, "recognizeImage: " + imageFilePath);
    // Parse out the folder path and file name
    int lastSlashIndex = imageFilePath.lastIndexOf("/");
    String folderPathImage = imageFilePath.substring(0, lastSlashIndex + 1);
    String imageFileName = imageFilePath.substring(lastSlashIndex + 1);

    // Resize the image so we can post it to camfind
    // (when the post completes, then we make another request for the recognition result of the just-posted image)
    String resizedImageFilePath = createResizedCameraCapturedImage(folderPathImage, imageFileName, 640, 480);
    postResizedImageToCamFind(resizedImageFilePath, imageFilePath);
  }

  private String createResizedCameraCapturedImage(String imageFolderPath, String imageFileName, int w_new, int h_new) {
    Log.d(TAG, "createResizedCameraCapturedImage");
    String originalImageFilePath = imageFolderPath + imageFileName;
    String resizedImageFilePath = imageFolderPath + CAMFIND_POST_IMAGE_FILE_NAME;

    Bitmap originalBitmap = BitmapFactory.decodeFile(originalImageFilePath);
    int originalBitmapWidth = originalBitmap.getWidth();
    int originalBitmapHeight = originalBitmap.getHeight();
    Bitmap resizedBitmap;
    if (originalBitmapWidth > originalBitmapHeight) {
      resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, w_new, h_new, false);
    } else {
      resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, h_new, w_new, false);
    }
    File resizedFile = new File(resizedImageFilePath);
    FileOutputStream fileOutputStream;
    try {
      fileOutputStream = new FileOutputStream(resizedFile);
      resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
      fileOutputStream.flush();
      fileOutputStream.close();
      originalBitmap.recycle();
      resizedBitmap.recycle();
    } catch (Exception e) {
    }

    return resizedImageFilePath;
  }

  private void postResizedImageToCamFind(final String resizedImageFilePath, final String originalImageFilePath) {
    Log.d(TAG, "postResizedImageToCamFind:  " + resizedImageFilePath);
    Runnable runnable = new Runnable() {
      public void run() {
        File file_resized = new File(resizedImageFilePath);
        try {
          HttpResponse<JsonNode> uploadImageRequest = Unirest.post("https://camfind.p.mashape.com/image_requests")
              .header("X-Mashape-Authorization", CAMFIND_API_KEY)
              .field("image_request[locale]", "en_US")
              .field("image_request[image]", file_resized)
              .asJson();
          String requestString = uploadImageRequest.getBody().toString();
          int firstCommaIndex = requestString.indexOf(",");
          String requestToken = requestString.substring(10, firstCommaIndex - 1);
          Log.d(TAG, "requestToken:  " + requestToken);
          onResizedImagePostedToCamFind(originalImageFilePath, requestToken);
        } catch (UnirestException e) {
          e.printStackTrace();
        }
      }
    };
    Thread thread = new Thread(runnable);
    thread.start();
  }

  private void askCamFindToRecognizeImage(final String originalImageFilePath, final String requestToken) {
    Log.d(TAG, "askCamFindToRecognizeImage");
    Handler delayHandler = new Handler();
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            HttpResponse<JsonNode> recognizeRequest = null;
            try {
              Log.d(TAG, "actually asking cam find to recognize image");
              recognizeRequest = Unirest.get("https://camfind.p.mashape.com/image_responses/" + requestToken)
                  .header("X-Mashape-Authorization", "YMQQG7yJ4LsBWIrmnzS19ErBtWOTMHlW")
                  .asJson();
            } catch (UnirestException e) {
              e.printStackTrace();
            }
            String recognitionString = recognizeRequest.getBody().toString();
            String recognizedObject = null;
            if (recognitionString.contains("not completed") == true) {
              Log.d(TAG, "askCamFindToRecognizeImage:  the server has not completed its image analyses");
            } else {
              recognizedObject = recognitionString.substring(30, recognitionString.length() - 2);
              Log.d(TAG, "recognizedObject:  " + recognizedObject);
            }
            mMainActivity.onImageRecognitionComplete(originalImageFilePath, recognizedObject);
          }
        };
        Thread thread = new Thread(runnable);
        thread.start();
      }
    };

    // Allow the server some time to recognize the posted image
    Log.d(TAG, "asking cam find to recognized posted image in " + String.valueOf(SERVER_SIDE_RECOGNITION_PROCESSING_TIME) + " seconds");
    long delay = SERVER_SIDE_RECOGNITION_PROCESSING_TIME;
    delayHandler.postDelayed(runnable, delay);
  }
}
