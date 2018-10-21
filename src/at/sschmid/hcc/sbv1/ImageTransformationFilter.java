package at.sschmid.hcc.sbv1;

import java.util.Arrays;

public class ImageTransformationFilter {
  
  public static int[][] GetTransformedImage(int[][] inImg, int width, int height, int[] transferFunction) {
    int[][] returnImg = new int[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        returnImg[x][y] = transferFunction[inImg[x][y]];
      }
    }
    return returnImg;
  }
  
  public static int[] GetInversionTF(int maxVal) {
    int[] transferFunction = new int[maxVal + 1];
    for (int i = 0; i < maxVal; i++) {
      transferFunction[i] = maxVal - i;
    }
    return transferFunction;
  }
  
  public static int[] GetHistogram(int maxVal, int[][] inImg, int width, int height) {
    int[] histogram = new int[maxVal + 1];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        histogram[inImg[x][y]]++;
      }
    }
    return histogram;
  }
  
  public static int[] GetGammaCorrTF(int maxVal, double gamma) {
    int[] transferFunction = new int[maxVal + 1];
    
    return transferFunction;
  }
  
  public static int[] GetBinaryThresholdTF(int maxVal, int tmin, int tmax, int FG_VAL, int BG_VAL) {
    int[] transferFunction = new int[maxVal + 1];
    for (int i = 0; i < maxVal; i++) {
      transferFunction[i] = i > tmin && i < tmax ? FG_VAL : BG_VAL;
    }
    
    return transferFunction;
  }
  
  // Bilder suchen, bei denen die Histogrameinebnung schlecht funktioniert
  // --> wenn ganze viele dunkelschwarze pxl vorkommen, da die ganzen Graustufen
  // ausgelassen werden m�ssen
  public static int[] GetHistogramEqualizationTF(int maxVal, int[][] inImg, int width, int height) {
    int[] returnTF = new int[maxVal + 1];
    
    // 250k pxl --> Erwartungswert je Histogram-Pos = ~1k (=width*height/256.0d)
//		int[] histogram = GetHistogram(maxVal, inImg, width, height);
//		int bIdx = 0;
//		int bSum = 1000;
//		int cumulatedSum = histogram[0];
//		returnTF[0] = 0; // black stays black
//		for (int i = 1; i < histogram.length; i++) {
//			cumulatedSum += histogram[i];
//			while (cumulatedSum > bSum) {
//				bIdx++;
//				bSum += 1000;
//			}
//			returnTF[i] = bIdx;
//		}

//		int expectation = (int) Math.floor(width * height / 256.0d);
    
    int[] histogram = GetHistogram(maxVal, inImg, width, height);
//		int[] histogram = new int[] { 100, 50, 0, 5, 100 };
    int expectation = (int) Math.floor(Arrays.stream(histogram)
        .sum() / (double) histogram.length);
    System.out.println("Expectation " + expectation + " Sum " + Arrays.stream(histogram)
        .sum() + " len " + histogram.length);
    int consumedPixel = histogram[0];
    int requiredPixel = expectation; // + consumedPixel;
    int nextColor = 0;
    returnTF[0] = nextColor;
    for (int i = 1; i < histogram.length; i++) {
      int histogramValue = histogram[i];
      consumedPixel += histogramValue;
      if (histogramValue > expectation / 2) {
        while (consumedPixel >= requiredPixel && nextColor < maxVal) {
          nextColor++;
          requiredPixel += expectation;
        }
      }
      returnTF[i] = nextColor;

//			System.out.println("i " + i + " h[i]" + histogram[i] + " cons " + consumedPixel + " req " + requiredPixel + "
// tfV " + transformValue);
    }
    
    // dummy output
//		for (int i = 0; i < histogram.length; i++) {
//			System.out.print(returnTF[i] + ", ");
//		}
//		System.out.println();
    
    return returnTF;
  }
  
  public static int[] GetHistogramEqualizationTF2(int maxVal, int[][] inImg, int width, int height) {
    int[] returnTF = new int[maxVal + 1];
    
    int[] histogram = GetHistogram(maxVal, inImg, width, height);
    int expectation = (int) Math.floor((Arrays.stream(histogram)
                                            .sum() - histogram[0]) / (double) (histogram.length - 1));
    
    int consumedPixel = 0;
    int requiredPixel = expectation;
    int nextColor = histogram[0] > expectation ? 1 : 0;
    
    returnTF[0] = 0;
    for (int i = 1; i < histogram.length; i++) {
      consumedPixel += histogram[i];
      returnTF[i] = nextColor;
      
      while (consumedPixel >= requiredPixel && nextColor < maxVal) {
        requiredPixel += expectation;
        nextColor++;
      }
    }
    
    return returnTF;
  }
  
  /**
   * Based on formula from lecture slides
   */
  public static int[] GetHistogramEqualizationTF3(int maxVal, int[][] inImg, int width, int height) {
    int[] returnTF = new int[maxVal + 1];
    int[] histogram = GetHistogram(maxVal, inImg, width, height);
    final int aMax = maxVal; // - 1;
    final int aMin = 0; // 1;
    final double totalPixels = width * height; // - histogram[0] - histogram[histogram.length - 1];
    final double[] probabilities = new double[histogram.length];

//		probabilities[0] = 0;
    int i = 0;
    for (; i < histogram.length /*- 1*/; i++) {
      int pixelsWithColor = histogram[i];
      probabilities[i] = pixelsWithColor / totalPixels;
    }
//		probabilities[i] = aMax;
    
    int range = (aMax - aMin); // + 1; // ((aMax - 1) - (aMin + 1)) + 1;
    double cumulatedProbability = 0; // probabilities[0];
    returnTF[0] = 0;
    for (i = 1 /*1*/; i < returnTF.length /*- 1*/; i++) {
      cumulatedProbability += probabilities[i];
      returnTF[i] = (int) Math.floor(cumulatedProbability * range) + aMin;
    }
    
    return returnTF;
  }
  
  /**
   * https://en.wikipedia.org/wiki/Histogram_equalization
   * <p>
   * https://www.codeproject.com/Tips/1172662/Histogram-Equalisation-in-Java https://stackoverflow
   * .com/questions/32015287/histogram-equalization-using-java
   */
  public static int[] GetHistogramEqualizationTF4(int maxVal, int[][] inImg, int width, int height) {
    final int imgSize = width * height;
    
    // create histogram
    int[] h = GetHistogram(maxVal, inImg, width, height);
    
    // create cumulative histogram
    int[] ch = new int[h.length];
    ch[0] = 0;
    for (int i = 1; i < maxVal; i++) {
      ch[i] = ch[i - 1] + h[i];
    }
    
    // prepare transform function
    int[] returnTF = new int[ch.length];
    returnTF[0] = 0;
    for (int i = 1; i < returnTF.length; i++) {
      returnTF[i] = Math.floorDiv(maxVal * ch[i], imgSize);
    }
    
    return returnTF;
  }
  
  /**
   * https://naushadsblog.wordpress.com/2014/02/10/image-processing-and-computer-vision-in-java-point-operators-part
   * -2histogram-equalization/
   */
  public static int[] GetHistogramEqualizationTF5(int maxVal, int[][] inImg, int width, int height) {
    int[] histogram = GetHistogram(maxVal, inImg, width, height);
    int[] returnTF = new int[histogram.length];
    final int imgSize = width * height;
    final float scale = (float) maxVal / imgSize;
    int sum = 0;
    for (int i = 0; i < histogram.length; i++) {
      sum += histogram[i];
      int color = Math.min((int) (scale * sum), maxVal);
      returnTF[i] = color;
    }
    
    return returnTF;
  }
  
}
