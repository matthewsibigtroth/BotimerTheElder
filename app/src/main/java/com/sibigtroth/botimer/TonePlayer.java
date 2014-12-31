package com.sibigtroth.botimer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

//modified version of code found at:
//http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android

public class TonePlayer {

  private int duration = 1; // seconds
  private int sampleRate = 8000;
  private int numSamples = duration * sampleRate;
  private double sample[] = new double[numSamples];
  private double freqOfTone = 440; // hz
  private byte generatedSnd[] = new byte[2 * numSamples];
  Handler handler = new Handler();
  private ArrayList<Tone> mTones;

  public TonePlayer() {
    mTones = new ArrayList<>();
  }

  ///////////////////////////
  //accessors
  ///////////////////////////


  ///////////////////////////
  //utilities
  ///////////////////////////

  public void clearTones() {
    mTones = new ArrayList<>();
  }

  public void createTone(int frequency, float duration) {
    Tone tone = new Tone(frequency, duration);
    mTones.add(tone);
  }

  public void playTone(int toneIndex) {
    mTones.get(toneIndex).play();
  }

  public void playToneAfterDelay(int toneIndex, int delay) {
    mTones.get(toneIndex).playAfterDelay(delay);
  }

  class Tone {
    private float duration; // seconds
    private int sampleRate;
    private int numSamples;
    private double[] sample;
    private double freqOfTone; // Hz
    private byte[] generatedSnd;
    private AudioTrack audioTrack;

    public Tone(int freqOfTone, float duration) {
      this.freqOfTone = freqOfTone;
      this.duration = duration;

      this.Init();
    }

    private void Init() {
      this.sampleRate = 8000;
      this.numSamples = (int) (duration * sampleRate);
      this.sample = new double[numSamples];
      this.generatedSnd = new byte[2 * numSamples];

      this.genTone();
      this.createAudioTrack();
    }

    public void play() {
      final Thread thread = new Thread(new Runnable() {
        public void run() {
          audioTrack.release();
          createAudioTrack();
          if (audioTrack.getState() == 1) {
            audioTrack.setStereoVolume(.3f, .3f);
            audioTrack.play();
          }
        }
      });
      thread.start();
    }

    public void playAfterDelay(final int delay) {
      final Thread thread = new Thread(new Runnable() {
        public void run() {
          //genTone();
          handler.postDelayed(new Runnable() {
            public void run() {
              //playSound();
              audioTrack.release();
              createAudioTrack();
              if (audioTrack.getState() == 1) {
                audioTrack.setStereoVolume(.3f, .3f);
                audioTrack.play();
              }

            }
          }, delay);
        }
      });
      thread.start();
    }

    void genTone() {
      // fill out the array
      for (int i = 0; i < numSamples; ++i) {
        sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
      }

      // convert to 16 bit pcm sound array
      // assumes the sample buffer is normalised.
      int idx = 0;
      int i = 0;

      int ramp = numSamples / 20; // Amplitude ramp as a percent of sample count


      for (i = 0; i < ramp; ++i) { // Ramp amplitude up (to avoid clicks)
        double dVal = sample[i];
        // Ramp up to maximum
        final short val = (short) ((dVal * 32767 * i / ramp));
        // in 16 bit wav PCM, first byte is the low order byte
        generatedSnd[idx++] = (byte) (val & 0x00ff);
        generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
      }


      for (i = i; i < numSamples - ramp; ++i) { // Max amplitude for most of the samples
        double dVal = sample[i];
        // scale to maximum amplitude
        final short val = (short) ((dVal * 32767));
        // in 16 bit wav PCM, first byte is the low order byte
        generatedSnd[idx++] = (byte) (val & 0x00ff);
        generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
      }

      for (i = i; i < numSamples; ++i) { // Ramp amplitude down
        double dVal = sample[i];
        // Ramp down to zero
        final short val = (short) ((dVal * 32767 * (numSamples - i) / ramp));
        // in 16 bit wav PCM, first byte is the low order byte
        generatedSnd[idx++] = (byte) (val & 0x00ff);
        generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
      }
    }

    void playSound() {
      Log.i("foo", "playing tone:  " + freqOfTone);

      AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
          sampleRate,
          AudioFormat.CHANNEL_CONFIGURATION_MONO,
          AudioFormat.ENCODING_PCM_16BIT,
          generatedSnd.length,
          AudioTrack.MODE_STATIC);

      audioTrack.write(generatedSnd, 0, generatedSnd.length);
      audioTrack.play();
    }

    private void createAudioTrack() {
      this.audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
          sampleRate,
          AudioFormat.CHANNEL_CONFIGURATION_MONO,
          AudioFormat.ENCODING_PCM_16BIT,
          generatedSnd.length,
          AudioTrack.MODE_STATIC);

      this.audioTrack.write(generatedSnd, 0, generatedSnd.length);
    }
  }

}






























