package at.sschmid.hcc.sbv1.image;

import java.util.logging.Logger;

public final class ImageTransferFunctions {
  
  private static final Logger LOGGER = Logger.getLogger(ImageTransferFunctions.class.getName());
  
  public static int[] GetInversionTF(int maxVal) {
    int[] transferFunction = new int[maxVal + 1];
    for (int i = 0; i < maxVal; i++) {
      transferFunction[i] = maxVal - i;
    }
    return transferFunction;
  }
  
  public static int[] GetGammaCorrTF(final int maxVal, final double gamma) {
    final int[] transferFunction = new int[maxVal + 1];
    
    return transferFunction;
  }
  
  // Bilder suchen, bei denen die Histogrameinebnung schlecht funktioniert
  // --> wenn ganze viele dunkelschwarze pxl vorkommen, da die ganzen Graustufen
  // ausgelassen werden müssen
  public static int[] GetHistogramEqualizationTF(final Image image) {
    int[] returnTF = new int[image.maxColor + 1];
    
    // 250k pxl --> Erwartungswert je Histogram-Pos = ~1k (=width*height/256.0d)
//		int[] histogram = GetHistogram(maxColor, inImg, width, height);
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
    
    int[] histogram = image.histogram().getData();
//		int[] histogram = new int[] { 100, 50, 0, 5, 100 };
    int expectation = (int) Math.floor((image.width * image.height) / (double) image.maxColor);
    LOGGER.info("Expectation " + expectation + " Sum " + image.size + " len " + histogram.length);
    int consumedPixel = histogram[0];
    int requiredPixel = expectation; // + consumedPixel;
    int nextColor = 0;
    returnTF[0] = nextColor;
    for (int i = 1; i < histogram.length; i++) {
      int histogramValue = histogram[i];
      consumedPixel += histogramValue;
      if (histogramValue > expectation / 2) {
        while (consumedPixel >= requiredPixel && nextColor < image.maxColor) {
          nextColor++;
          requiredPixel += expectation;
        }
      }
      returnTF[i] = nextColor;

//			LOGGER.info("i " + i + " h[i]" + histogram[i] + " cons " + consumedPixel + " req " + requiredPixel + "
// tfV " + transformValue);
    }
    
    return returnTF;
  }
  
  /**
   * Based on implementation idea from the whiteboard (moving bars from the histogram).
   */
  public static int[] GetHistogramEqualizationTF2(final Image image) {
    int[] returnTF = new int[image.maxColor + 1];
  
    int[] histogram = image.histogram().getData();
    int expectation = (int) Math.floor((image.size - histogram[0]) / (double) (histogram.length - 1));
    
    int consumedPixel = 0;
    int requiredPixel = expectation;
    int nextColor = histogram[0] > expectation ? 1 : 0;
    
    returnTF[0] = 0;
    for (int i = 1; i < histogram.length; i++) {
      consumedPixel += histogram[i];
      returnTF[i] = nextColor;
      
      while (consumedPixel >= requiredPixel && nextColor < image.maxColor) {
        requiredPixel += expectation;
        nextColor++;
      }
    }
    
    return returnTF;
  }
  
  /**
   * Based on formula from lecture slides
   */
  public static int[] GetHistogramEqualizationTF3(final Image image) {
    int[] returnTF = new int[image.maxColor + 1];
    int[] histogram = image.histogram().getData();
    final int aMax = image.maxColor; // - 1;
    final int aMin = 0; // 1;
    final double totalPixels = image.size; // - histogram[0] - histogram[histogram.length - 1];
    final double[] probabilities = new double[histogram.length];

//		probabilities[0] = 0;
    int i = 0;
    for (; i < histogram.length /*- 1*/; i++) {
      int pixelsWithColor = histogram[i];
      probabilities[i] = pixelsWithColor / totalPixels;
    }
//		probabilities[i] = aMax;
    
    int range = aMax - aMin + 1; // ((aMax - 1) - (aMin + 1)) + 1;
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
  public static int[] GetHistogramEqualizationTF4(final Image image) {
    // create histogram
    int[] histogram = image.histogram().getData();
    
    // create cumulative histogram
    int[] ch = new int[histogram.length];
    ch[0] = 0;
    for (int i = 1; i < image.maxColor; i++) {
      ch[i] = ch[i - 1] + histogram[i];
    }
    
    // prepare transform function
    int[] returnTF = new int[ch.length];
    returnTF[0] = 0;
    for (int i = 1; i < returnTF.length; i++) {
      returnTF[i] = Math.floorDiv(image.maxColor * ch[i], image.size);
    }
    
    return returnTF;
  }
  
  /**
   * https://naushadsblog.wordpress.com/2014/02/10/image-processing-and-computer-vision-in-java-point-operators-part
   * -2histogram-equalization/
   */
  public static int[] GetHistogramEqualizationTF5(final Image image) {
    int[] histogram = image.histogram().getData();
    int[] returnTF = new int[histogram.length];
    final float scale = (float) image.maxColor / image.size;
    int sum = 0;
    for (int i = 0; i < histogram.length; i++) {
      sum += histogram[i];
      int color = Math.min((int) (scale * sum), image.maxColor);
      returnTF[i] = color;
    }
    
    return returnTF;
  }
  
}
