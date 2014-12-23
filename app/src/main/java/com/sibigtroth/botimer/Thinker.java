package com.sibigtroth.botimer;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by sibigtroth on 12/23/14.
 */
public class Thinker {

  private MainActivity mMainActivity;

  public Thinker(MainActivity mainActivity) {
    mMainActivity = mainActivity;

    initialize();
  }

  private void initialize() {
  }


  ///////////////////////////
  // callbacks
  ///////////////////////////

  public interface ThinkerCallback {

    public void onThinkingDone(String speechResponse);
  }


  ///////////////////////////
  // utilities
  ///////////////////////////

  public void sayToBot(String textToSpeak) {
    Log.d("foo", "SayToBot:   " + textToSpeak);
    //this.GetBrainActivity().GetListener().StopListening();

    final String textToSpeak_ = textToSpeak;
    //this.thinkingDisplay.ShowThinkingIndicator();

    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

      @Override
      protected void onPreExecute() {
        //Show UI
      }

      @Override
      protected Void doInBackground(Void... arg0) {
        HttpURLConnection connection;
        OutputStreamWriter request = null;

        URL url = null;
        String response = null;

        //String urlString = "http://www.pandorabots.com/pandora/talk-xml?botid=e365655dbe351ac7&input=hello";
        String urlString = "http://www.pandorabots.com/pandora/talk-xml?botid=e365655dbe351ac7&input=" + Uri.encode(textToSpeak_);

        try {
          url = new URL(urlString);
        } catch (MalformedURLException e) {
        }

        try {
          connection = (HttpURLConnection) url.openConnection();
          connection.setDoOutput(true);
          connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
          connection.setRequestMethod("POST");
          request = new OutputStreamWriter(connection.getOutputStream());

          try {
            request.flush();
            request.close();
          } catch (IOException e) {
          }
          String line = "";
          InputStreamReader isr = new InputStreamReader(connection.getInputStream());
          BufferedReader reader = new BufferedReader(isr);
          StringBuilder sb = new StringBuilder();
          while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
          }
          // Response from server after login process will be stored in response variable.
          response = sb.toString();

          isr.close();
          reader.close();

          int index_start = response.indexOf("<that>") + 6;
          int index_stop = response.indexOf("</that>");
          String speechResponse = response.substring(index_start, index_stop);

          //GetBrainActivity().GetSpeaker().Speak(speechResponse);
          mMainActivity.onThinkingDone(speechResponse);
        } catch (IOException e) {
          Log.d("foo", e.getMessage());
        }
        //thinkingDisplay.HideThinkingIndicator();
        return null;
      }

      @Override
      protected void onPostExecute(Void result) {
        //Show UI (Toast msg here)
      }

    };

    task.execute((Void[]) null);
  }
}
