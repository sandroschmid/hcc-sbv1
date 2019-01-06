package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.image.segmentation.OptimalThreshold;

import java.util.Arrays;

public final class Histogram {
  
  private final Image image;
  private final int[] data;
  private final int count;
  
  private double[] probabilities;
  private boolean calculatedStatistics;
  private int minOccurringColor;
  private int maxOccurringColor;
  private int minColor;
  private int maxColor;
  private double averageColor;
  
  public Histogram(final Image image) {
    this.image = image;
    this.count = image.size;
    this.data = new int[image.maxColor + 1];
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        this.data[image.data[x][y]]++;
      }
    }
  }
  
  public Image getImage() {
    return image;
  }
  
  public int[] getData() {
    return data;
  }
  
  public double[] getProbabilities() {
    if (probabilities == null) {
      probabilities = new double[data.length];
      final double totalPixels = (double) image.size;
      for (int i = 0; i < probabilities.length; i++) {
        probabilities[i] = data[i] / totalPixels;
      }
    }
    
    return probabilities;
  }
  
  public OptimalThreshold optimalThreshold(final int version) {
    return new OptimalThreshold(this, version);
  }
  
  public int getMinOccurringColor() {
    calculateStatistics();
    return minOccurringColor;
  }
  
  public int getMaxOccurringColor() {
    calculateStatistics();
    return maxOccurringColor;
  }
  
  public int getMinColor() {
    calculateStatistics();
    return minColor;
  }
  
  public int getMaxColor() {
    calculateStatistics();
    return maxColor;
  }
  
  public double getAverageColor() {
    calculateStatistics();
    return averageColor;
  }
  
  @Override
  public String toString() {
    calculateStatistics();
    
    final StringBuilder builder = new StringBuilder("Histogram");
    if (image.hasName()) {
      builder.append(" (").append(image.getName()).append(")");
    }
  
    return builder.append(" {\n  count=")
        .append(count)
        .append(",\n  minColor=")
        .append(minColor)
        .append(",\n  maxColor=")
        .append(maxColor)
        .append(",\n  averageColor=")
        .append(averageColor)
        .append(",\n  minOccurringColor=")
        .append(minOccurringColor)
        .append(" (")
        .append(data[minOccurringColor])
        .append("),\n  maxOccurringColor=")
        .append(maxOccurringColor)
        .append(" (")
        .append(data[maxOccurringColor])
        .append("),\n  data=")
        .append(Arrays.toString(data))
        .append("\n}")
        .toString();
  }
  
  private void calculateStatistics() {
    if (!calculatedStatistics) {
      minOccurringColor = 0;
      maxOccurringColor = 0;
      minColor = image.maxColor + 1;
      maxColor = -1;
      averageColor = 0d;
      
      int occurringColors = 0;
  
      for (int color = 0; color <= image.maxColor; color++) {
        final int occurrences = data[color];
    
        if (occurrences > 0) {
          if (occurrences < data[minOccurringColor]) {
            minOccurringColor = color;
          }
      
          if (occurrences > data[maxOccurringColor]) {
            maxOccurringColor = color;
          }
      
          occurringColors += occurrences;
          averageColor += occurrences * color;
          
          if (color < minColor) {
            minColor = color;
          }
          
          if (color > maxColor) {
            maxColor = color;
          }
        }
      }
      
      averageColor /= (double) occurringColors;
      calculatedStatistics = true;
    }
  }
  
}
