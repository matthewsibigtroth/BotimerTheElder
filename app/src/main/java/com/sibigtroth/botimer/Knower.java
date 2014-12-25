package com.sibigtroth.botimer;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by sibigtroth on 12/23/14.
 */
public class Knower {

  private MainActivity mMainActivity;
  public ArrayList<String> HOT_PHRASES;

  public Knower(MainActivity mainActivity) {
    mMainActivity = mainActivity;

    initialize();
  }

  private void initialize() {
    createKnowledgeHotPhrases();
  }

  private void createKnowledgeHotPhrases() {
    HOT_PHRASES = new ArrayList<>(Arrays.asList(
        "what is",
        "what are",
        "show me",
        "what do you know about",
        "who is",
        "where is",
        "tell me about",
        "have you ever heard of"
    ));
  }


  /////////////////////////////////////
  //callbacks
  /////////////////////////////////////

  public interface KnowerCallback {

    public void onFreebaseNodeDataFound(FreebaseNodeData FreebaseNodeData, String inputText);

    public void onRelatedFreebaseNodeDataFound(FreebaseNodeData FreebaseNodeData, String inputText);

  }

  /////////////////////////////////////
  //utilities
  /////////////////////////////////////

  public void findFreebaseNodeDataForInputText(String inputText) {
    final String inputText_ = inputText.replace(" ", "_").toString();
    new Thread(new Runnable() {

      String inputText__ = inputText_;

      @Override
      public void run() {
        try {
          JSONArray TopicData = findTopicDataForInputText(inputText__);

          if (TopicData.length() == 0) {
            mMainActivity.onFreebaseNodeDataFound(null, inputText__);
          } else {
            JSONObject TopicDatum = new JSONObject(TopicData.get(0).toString());
            FreebaseNodeData freebaseNodeData = createFreebaseNodeDataForTopicDatum(TopicDatum);
            mMainActivity.onFreebaseNodeDataFound(freebaseNodeData, inputText__);
          }

        } catch (IOException e) {
          e.printStackTrace();
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  public void findRelatedFreebaseNodeDataForInputText(String inputText) {
    final String inputText_ = inputText.replace(" ", "_").toString();
    new Thread(new Runnable() {

      String inputText__ = inputText_;

      @Override
      public void run() {
        try {
          JSONArray TopicData = findTopicDataForInputText(inputText__);
          int numTopics = TopicData.length();
          if (numTopics > 0) {
            int index_rand = new Random().nextInt(numTopics);
            JSONObject TopicDatum = new JSONObject(TopicData.get(index_rand).toString());
            //JSONObject TopicDatum = new JSONObject(TopicData.get(1).toString());
            FreebaseNodeData freebaseNodeData = createFreebaseNodeDataForTopicDatum(TopicDatum);
            //OnComplete_findFreebaseNodeDataForInputText(FreebaseNodeData, inputText__);
            mMainActivity.onRelatedFreebaseNodeDataFound(freebaseNodeData, inputText__);
          }
        } catch (IOException e) {
          e.printStackTrace();
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  private JSONArray findTopicDataForInputText(String inputText) throws IOException, JSONException {
    HttpTransport httpTransport = new NetHttpTransport();
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();

    String query = inputText.toString();
    GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/search");
    url.put("key", "AIzaSyAhwf40hmgjrTc57ije8rqorJ6x-8hKFXE");
    url.put("query", query);

    HttpRequest request = requestFactory.buildGetRequest(url);
    HttpResponse httpResponse = request.execute();
    String json = httpResponse.parseAsString();

    JSONObject Blob = new org.json.JSONObject(json);
    JSONArray TopicData = Blob.getJSONArray("result");

    for (int i = 0; i < TopicData.length(); i++) {
      JSONObject TopicDatum = new JSONObject(TopicData.get(i).toString());
      String name = TopicDatum.get("name").toString();
    }

    return TopicData;
  }

  private FreebaseNodeData createFreebaseNodeDataForTopicDatum(JSONObject TopicDatum) throws JSONException, IOException {
    //get the topic id
    String id_topic = "";
    try {
      id_topic = TopicDatum.get("id").toString();
    } catch (Exception e) {
      id_topic = TopicDatum.get("mid").toString().replace("\\", "");
    }

    //get the topic name
    String name = TopicDatum.get("name").toString();
    Log.d("foo", "CreateFreebaseNodeDataForTopicDatum:   " + name);

    //get an image for this topic
    String url_image = this.findImageForTopic(id_topic);
    Log.d("foo", url_image);

    //get the article text for this topic
    String text = this.findTextForTopic(id_topic);

    //package the data
    FreebaseNodeData FreebaseNodeData = new FreebaseNodeData(name, id_topic, url_image, text);

    return FreebaseNodeData;
  }

  private String findImageForTopic(String id_topic) throws IOException, JSONException {
    String url_base = "https://www.googleapis.com/freebase/v1/topic";

    String param_key = "key=AIzaSyAhwf40hmgjrTc57ije8rqorJ6x-8hKFXE";
    String param_filter = "filter=/common/topic/image&limit=10";

    String url = url_base + id_topic + "?" + param_key + "&" + param_filter;

    GenericUrl GenericUrl = new GenericUrl(url);
    HttpTransport httpTransport = new NetHttpTransport();
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
    HttpRequest request = requestFactory.buildGetRequest(GenericUrl);
    HttpResponse httpResponse = request.execute();
    String json = httpResponse.parseAsString();

    String url_image = "";
    try {
      JSONObject Blob = new org.json.JSONObject(json);
      JSONObject Property = Blob.getJSONObject("property");
      JSONObject Common_topic_image = Property.getJSONObject("/common/topic/image");
      JSONArray Values = Common_topic_image.getJSONArray("values");

      int numImages = Values.length();
      int index_rand = new Random().nextInt(numImages);
      JSONObject Value = Values.getJSONObject(index_rand);
      String id_image = Value.get("id").toString();
      String url_base_image = "https://usercontent.googleapis.com/freebase/v1/image";
      int maxwidth = 1000;
      int maxheight = 500;
      String params = "?maxwidth=" + String.valueOf(maxwidth) + "&maxheight=" + String.valueOf(maxheight) + "&mode=fillcropmid";
      url_image = url_base_image + id_image + params;
    } catch (Exception e) {
      url_image = "";
    }

    return url_image;
  }

  private boolean determineIfImageExists(String url_image) throws IOException {
    HashMap<String, Integer> ImageDimensions = this.determineImageDimensionsFromUrl(url_image);
    int w_image = ImageDimensions.get("w");
    int h_image = ImageDimensions.get("h");

    int NULL_IMAGE_SIZE = 301;

    if ((w_image == NULL_IMAGE_SIZE) && (h_image == NULL_IMAGE_SIZE)) {
      return false;
    } else {
      return true;
    }
  }

  private HashMap<String, Integer> determineImageDimensionsFromUrl(String url) throws IOException {

    InputStream is = (InputStream) new URL(url).getContent();
    Drawable d = Drawable.createFromStream(is, "imagename");
    Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
    int w_image = bitmap.getWidth();
    int h_image = bitmap.getHeight();
    HashMap<String, Integer> ImageDimensions = new HashMap<String, Integer>();
    ImageDimensions.put("w", w_image);
    ImageDimensions.put("h", h_image);
    return ImageDimensions;
  }

  private String findTextForTopic(String id_topic) throws IOException, JSONException {
    String text = "";
    try {
      String url_base = "https://www.googleapis.com/freebase/v1/text";
      String url_base_withTopicId = url_base + id_topic;
      GenericUrl url = new GenericUrl(url_base_withTopicId);
      url.put("key", "AIzaSyAhwf40hmgjrTc57ije8rqorJ6x-8hKFXE");
      HttpTransport httpTransport = new NetHttpTransport();
      HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
      HttpRequest request = requestFactory.buildGetRequest(url);
      HttpResponse httpResponse = request.execute();
      String json = httpResponse.parseAsString();
      JSONObject Blob = new org.json.JSONObject(json);
      text = Blob.get("result").toString();
    } catch (Exception e) {
      text = "";
    }
    return text;
  }

  class FreebaseNodeData {
    public String name;
    public String id_topic;
    public String url_image;
    public String text;

    public FreebaseNodeData(String name, String id_topic, String url_image, String text) {
      this.name = name;
      this.id_topic = id_topic;
      this.url_image = url_image;
      this.text = text;
    }
  }

}