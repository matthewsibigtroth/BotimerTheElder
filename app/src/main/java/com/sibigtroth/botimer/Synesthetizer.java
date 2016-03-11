package com.sibigtroth.botimer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by sibigtroth on 12/25/14.
 */
public class Synesthetizer {

  private static final String TAG = "Synesthetizer";
  private MainActivity mMainActivity;
  public ArrayList<String> HOT_PHRASES;
  private ArrayList<Cluster> mRepresentativeClusters;
  private Random mRandom;
  public String CAPTURED_SYNESTHETIZER_IMAGE_FILE_PATH;
  private static final int SYNESTHETIZER_PALETTE_SIZE = 7;
  private static final float[] C_MAJOR_SCALE = new float[]{32.7032f, 36.7081f, 41.2034f, 43.6535f, 48.9994f, 55.0000f, 61.7354f}; // C, D, E, F, G, A, B
  private static final int MIN_HARMONIC = 9;
  private static final int MAX_HARMONIC = 14;

  public Synesthetizer(MainActivity mainActivity) {
    mMainActivity = mainActivity;
    createSynesthesiaHotPhrases();
    CAPTURED_SYNESTHETIZER_IMAGE_FILE_PATH = mMainActivity.getExternalFilesDir(null).getAbsolutePath() + "/synesthetizerCapturedImage.jpg";
  }

  public interface SynesthetizerCallback {
    public void onSynesthetizerImagePaletteExtracted(ArrayList<PaletteColor> paletteColors);
  }

  private void createSynesthesiaHotPhrases() {
    HOT_PHRASES = new ArrayList<>(Arrays.asList(
        "play what you see"
    ));
  }

  public void synesthetizeImage(String imageFilePath) {
    ArrayList<PaletteColor> paletteColors = determineImagePalette(imageFilePath, SYNESTHETIZER_PALETTE_SIZE);
    mMainActivity.onSynesthetizerImagePaletteExtracted(paletteColors);
  }

  public Point findRepresentativeClusterPointForGivenClusterIndex(int clusterIndex) {
    Point point = null;
    ArrayList<Point> representativeClusterPoints = mRepresentativeClusters.get(clusterIndex).mPoints;
    if (representativeClusterPoints.size() > 0) {
      int randIndex = mRandom.nextInt(representativeClusterPoints.size());
      point = representativeClusterPoints.get(randIndex);
    }

    return point;
  }

  public ArrayList<PaletteColor> determineImagePalette(String imageFilePath, int paletteSize) {
    // Scale down the original bitmap to save on cluster finding time
    float resizeScale = .1f;
    Bitmap resizedBitmap = resizeImage(imageFilePath, resizeScale);
    BitmapPixel[] resizedBitmapPixels = collectBitmapPixels(resizedBitmap);
    int numPixels = resizedBitmapPixels.length;
    // Collect the pixels and their color for all pixels in the resized bitmap
    Point[] points = new Point[numPixels];
    for (int i = 0; i < numPixels; i++) {
      int pixelColor = resizedBitmapPixels[i].color;
      int r = Color.red(pixelColor);
      int g = Color.green(pixelColor);
      int b = Color.blue(pixelColor);
      // Make sure to scale back up the pixel coords (since we scaled down the original image when collecting pixels)
      float fullSizeBitmapX = ((float) resizedBitmapPixels[i].x / resizeScale);
      float fullSizeBitmapY = ((float) resizedBitmapPixels[i].y / resizeScale);
      Point point = new Point(r, g, b, (int) fullSizeBitmapX, (int) fullSizeBitmapY);
      points[i] = point;
    }
    // Specify the number of clusters to search for
    int numClusters = paletteSize;
    // Look for clusters in rgb space
    int width = 255;
    int height = 255;
    int depth = 255;
    // Find the clusters
    DalvikClusterer dalvikClusterer = new DalvikClusterer();
    Point[] rgbColors = dalvikClusterer.cluster(points, numClusters, width, height, depth);
    // Collect the colors into a palette
    ArrayList<PaletteColor> paletteColors = new ArrayList<>();
    for (int j = 0; j < rgbColors.length; j++) {
      int r = rgbColors[j].x;
      int g = rgbColors[j].y;
      int b = rgbColors[j].z;
      int color = Color.rgb(r, g, b);
      int frequency = mapColorToFrequency(color);
      paletteColors.add(new PaletteColor(color, j, frequency));
    }
    // Store the clusters for later retrieval
    storeRepresentativeClusters(points, dalvikClusterer.means, dalvikClusterer);

    return paletteColors;
  }

  private void storeRepresentativeClusters(Point[] points, Point[] means, DalvikClusterer dalvikClusterer) {
    // Create the empty clusters
    mRepresentativeClusters = new ArrayList<>();
    for (int j = 0; j < means.length; j++) {
      ArrayList<Point> clusterPoints = new ArrayList<>();
      Point mean = means[j];
      Cluster cluster = new Cluster(clusterPoints, mean);
      mRepresentativeClusters.add(cluster);
    }
    double distanceThreshold = 25;//10;
    // Loop through all the points
    for (int i = 0; i < points.length; i++) {
      // Get the distance between this point and its cluster mean
      int clusterForThisPoint = points[i].cluster;
      Point meanForThisCluster = means[clusterForThisPoint];
      double distanceBetweenThisPointAndClusterMean = dalvikClusterer.computeDistance(points[i], meanForThisCluster);
      // If this distance is small enough
      if (distanceBetweenThisPointAndClusterMean <= distanceThreshold) {
        // Add it to the stored representative cluster
        mRepresentativeClusters.get(clusterForThisPoint).mPoints.add(points[i]);
      }
    }
  }

  class Cluster {

    ArrayList<Point> mPoints;
    Point mMean;

    public Cluster(ArrayList<Point> points, Point mean) {
      mPoints = points;
      mMean = mean;
    }
  }

  public Bitmap resizeImage(String imageFilePath, float newScale) {
    Bitmap bitmap_orig = BitmapFactory.decodeFile(imageFilePath);
    int bitmapOrigWidth = bitmap_orig.getWidth();
    int bitmapOrigHeight = bitmap_orig.getHeight();
    int newBitmapWidth = (int) (bitmapOrigWidth * newScale);
    int newBitmapHeight = (int) (bitmapOrigHeight * newScale);
    Bitmap bitmap_resized;
    bitmap_resized = Bitmap.createScaledBitmap(bitmap_orig, newBitmapWidth, newBitmapHeight, false);

    return bitmap_resized;
  }

  private BitmapPixel[] collectBitmapPixels(Bitmap bitmap) {
    int bitmapWidth = bitmap.getWidth();
    int bitmapHeight = bitmap.getHeight();
    int numPixels = bitmapWidth * bitmapHeight;
    BitmapPixel[] bitmapPixels = new BitmapPixel[numPixels];

    int pixelCounter = 0;
    for (int x = 0; x < bitmapWidth; x++) {
      for (int y = 0; y < bitmapHeight; y++) {
        int color = bitmap.getPixel(x, y);
        bitmapPixels[pixelCounter] = new BitmapPixel(color, x, y);
        pixelCounter += 1;
      }
    }

    return bitmapPixels;
  }

  class BitmapPixel {
    public int color;
    public int x;
    public int y;

    public BitmapPixel(int color, int x, int y) {
      this.color = color;
      this.x = x;
      this.y = y;
    }
  }

  /*
  // Version 1
  // Linearly maps rgb to frequency
  // f = (normalizedHue * numNotesInAScale) * value
  private int mapColorToFrequency(int color) {
    //convert color to hsv
    float[] hsv = new float[3];
    Color.colorToHSV(color, hsv);
    float hue = hsv[0];
    float value = hsv[2];

    int numNotesInAScale = 12;

    // Calculate the hue component (this gives the note)
    float maxHue = 360f;
    float hue_normalized = hue / maxHue;
    int hue_component = (int) (hue_normalized * numNotesInAScale);

    // Calculate the value component (this gives the scale)s
    int value_component_min = 3;
    int value_component_max = 7;
    int value_component = (int) (((value * (value_component_max - value_component_min)) + value_component_min) * numNotesInAScale);

    // Determine the associated piano key
    int pianoKey = hue_component + value_component;

    // Determine that piano key's frequency
    float exponent = (pianoKey - 49) / 12f;
    int frequency = (int) (Math.pow(2, exponent) * 440);

    return frequency;
  }
  */

  // Version 2
  // Uses a given scale for the mapping
  // Hue is used to determine the note in the scale
  // Value is used to determine the harmonic of the note
  // f = note * harmonic
  private int mapColorToFrequency(int color) {
    //convert color to hsv
    float[] hsv = new float[3];
    Color.colorToHSV(color, hsv);
    float hue = hsv[0];
    float value = hsv[2];

    // Calculate the note
    float maxHue = 360f;
    float normalizedHue = hue / maxHue;
    int numNotesInScale = C_MAJOR_SCALE.length;
    int noteIndex = (int)(normalizedHue * numNotesInScale);
    float note = C_MAJOR_SCALE[noteIndex];

    // Calculate the harmonic
    int minHarmonic = MIN_HARMONIC;
    int maxHarmonic = MAX_HARMONIC;
    int harmonic = (int) (((value * (maxHarmonic - minHarmonic)) + minHarmonic));

    // Calculate the frequency
    int frequency = (int)(note * harmonic);

    return frequency;
  }

  //edited code based on that found at:
  //https://code.google.com/p/hdict/source/browse/src/com/google/io/kmeans/?r=66e5aa096d9b323ac685a41165aa668d90819df5
  public class DalvikClusterer {
    private static final int MAX_LOOP_COUNT = 10;//15;
    private double[] distances;
    private final Random random = new Random(System.currentTimeMillis());
    public Point[] means;

    public Point[] cluster(Point[] points, int numClusters, int width, int height, int depth) {
      Log.d("foo", "start clustering");

      boolean converged = false;
      boolean dirty;
      double distance;
      double curMinDistance;
      int loopCount = 0;
      Point point;
      distances = new double[points.length];

      // Randomly pick some points to be the centroids of the groups, for the first pass
      this.means = new Point[numClusters];
      for (int i = 0; i < numClusters; ++i) {
        //means[i] = new Point(random.nextInt(width), random.nextInt(height), random.nextInt(depth));
        int index_randPoint = this.random.nextInt(points.length);
        Point point_rand = points[index_randPoint];
        means[i] = new Point(point_rand.x, point_rand.y, point_rand.z, -1, -1);
        means[i].cluster = i;
      }

      // Initialize data
      for (int i = 0; i < points.length; ++i) {
        distances[i] = Double.MAX_VALUE;
      }
      int[] sumX = new int[numClusters];
      int[] sumY = new int[numClusters];
      int[] sumZ = new int[numClusters];
      int[] clusterSizes = new int[numClusters];

      // Main loop
      while (!converged) {
        dirty = false;
        // compute which group each point is closest to
        for (int i = 0; i < points.length; ++i) {
          point = points[i];
          curMinDistance = distances[i];
          for (Point mean : means) {
            distance = computeDistance(point, mean);
            if (distance < curMinDistance) {
              dirty = true;
              distances[i] = distance;
              curMinDistance = distance;
              point.cluster = mean.cluster;
            }
          }
        }

        // If we did no work, break early (greedy algorithm has converged)
        if (!dirty) {
          converged = true;
          break;
        }

        // Compute the new centroids of the groups, since contents have changed
        for (int i = 0; i < numClusters; ++i) {
          sumX[i] = sumY[i] = sumZ[i] = clusterSizes[i] = 0;
        }

        for (int i = 0; i < points.length; ++i) {
          point = points[i];
          sumX[point.cluster] += point.x;
          sumY[point.cluster] += point.y;
          sumZ[point.cluster] += point.z;
          clusterSizes[point.cluster] += 1;
        }

        for (int i = 0; i < numClusters; ++i) {
          // Matt added this if statement to account for bigger numClusters
          // For big numClusters, it's possible that some clusters don't have any points associated
          // So their clusterSize is zero, which leads to the division by zero below
          if (clusterSizes[i] != 0) {
            means[i].x = (int) (sumX[i] / clusterSizes[i]);
            means[i].y = (int) (sumY[i] / clusterSizes[i]);
            means[i].z = (int) (sumZ[i] / clusterSizes[i]);
          }
        }

        // Bail out after at most MAX_LOOP_COUNT passes
        loopCount++;

        converged = converged || (loopCount > MAX_LOOP_COUNT);
      }

      for (int i = 0; i < numClusters; ++i) {
        if (clusterSizes[i] == 0) {
          means[i].x = 0;
          means[i].y = 0;
          means[i].z = 0;
        }
      }

      return means;
    }

    //Computes the Cartesian distance between two points.
    private double computeDistance(Point a, Point b) {
      return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) + (a.z - b.z) * (a.z - b.z));
    }
  }

  public static class Point {
    public int x;
    public int y;
    public int z;
    public int cluster;
    public int x_pixel;
    public int y_pixel;

    public Point(int d, int e, int f, int x_pixel, int y_pixel) {
      this.x = d;
      this.y = e;
      this.z = f;
      this.x_pixel = x_pixel;
      this.y_pixel = y_pixel;
    }
  }

  public class PaletteColor {
    public Integer color;
    public int clusterIndex;
    public int toneFrequency;

    public PaletteColor(Integer paletteColor, int paletteClusterIndex, int paletteToneFrequency) {
      color = paletteColor;
      clusterIndex = paletteClusterIndex;
      toneFrequency = paletteToneFrequency;
    }
  }
}
