package at.sschmid.hcc.sbv1.image;

import java.util.Arrays;

public final class Histogram {
  
  private final Image image;
  private final int[] data;
  private final int count;
  
  private double[] probabilities;
  private boolean calculatedStatistics;
  private int minOccurringColor;
  private int maxOccurringColor;
  private double averageOccurrences;
  private int minColor;
  private int maxColor;
  private double averageColor;
  
  Histogram(final Image image) {
    this.image = image;
    this.count = image.size;
    this.data = new int[image.maxColor + 1];
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        this.data[image.data[x][y]]++;
      }
    }
  }
  
  public int[] getData() {
    return data;
  }
  
  public double[] getProbabilities() {
    if (probabilities == null) {
      probabilities = new double[data.length];
      final double total = (double) image.size;
      for (int i = 0; i < probabilities.length; i++) {
        probabilities[i] = data[i] / total;
      }
    }
  
    return probabilities;
  }
  
  public int getMinOccurringColor() {
    calculateStatistics();
    return minOccurringColor;
  }
  
  public int getMaxOccurringColor() {
    calculateStatistics();
    return maxOccurringColor;
  }
  
  public double getAverageOccurrences() {
    calculateStatistics();
    return averageOccurrences;
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
      builder.append(" (").append(image.name).append(")");
    }
    
    return builder.append(" { count=")
        .append(count)
        .append(", minColor=")
        .append(minColor)
        .append(", maxColor=")
        .append(maxColor)
        .append(", averageColor=")
        .append(averageColor)
        .append(", minOccurringColor=")
        .append(minOccurringColor)
        .append(" (")
        .append(data[minOccurringColor])
        .append("), maxOccurringColor=")
        .append(maxOccurringColor)
        .append(" (")
        .append(data[maxOccurringColor])
        .append("), averageOccurrences=")
        .append(averageOccurrences)
        .append(", data=")
        .append(Arrays.toString(data))
        .append(" }")
        .toString();
  }
  
  private void calculateStatistics() {
    if (!calculatedStatistics) {
      minOccurringColor = image.size;
      maxOccurringColor = -1;
      averageOccurrences = 0d;
      minColor = image.maxColor;
      maxColor = -1;
      averageColor = 0d;
      
      int occurringColors = 0;
      
      for (int color = 0; color < image.maxColor; color++) {
        final int occurrences = data[color];
        
        averageOccurrences += occurrences;
        if (occurrences < minOccurringColor) {
          minOccurringColor = color;
        }
        
        if (occurrences > maxOccurringColor) {
          maxOccurringColor = color;
        }
        
        if (occurrences > 0) {
        	occurringColors++;
          averageColor += color;
          
          if (color < minColor) {
            minColor = color;
          }
          
          if (color > maxColor) {
            maxColor = color;
          }
        }
      }
      
      averageOccurrences /= (double) (image.maxColor + 1);
      averageColor /= (double) occurringColors;
      
      calculatedStatistics = true;
    }
  }
  
}
