package at.sschmid.hcc.sbv1.image;

import java.util.Arrays;

public final class Histogram2d {
  
  private final Image image1;
  private final Image image2;
  private final int[][] data;
  private final int count;
  
  private double[][] probabilities;
  private boolean calculatedStatistics;
  private int minOccurringColor;
  private int maxOccurringColor;
  private double averageOccurrences;
  private int minColor;
  private int maxColor;
  private double averageColor;
  
  public Histogram2d(final Image image1, final Image image2) {
    if (!image1.sizeEqualsTo(image2)) {
      throw new IllegalArgumentException("Images for 2d histograms must be of equal sizes");
    }
    
    this.image1 = image1;
    this.image2 = image2;
    this.count = image1.size;
    this.data = new int[image1.maxColor + 1][image1.maxColor + 1];
    for (int x = 0; x < image1.width; x++) {
      for (int y = 0; y < image1.height; y++) {
        this.data[image1.data[x][y]][image2.data[x][y]]++;
      }
    }
  }
  
  public int[][] getData() {
    return data;
  }
  
  public double[][] getProbabilities() {
    if (probabilities == null) {
      probabilities = new double[data.length][data.length];
      final double total = (double) image1.size * image1.size;
      for (int i = 0; i < probabilities.length; i++) {
        for (int j = 0; j < probabilities.length; j++) {
          probabilities[i][j] = data[i][j] / total;
        }
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
    if (image1.hasName() && image2.hasName()) {
      builder.append(" (").append(image1.name).append("-").append(image2.name).append(")");
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
      minOccurringColor = image1.size;
      maxOccurringColor = -1;
      averageOccurrences = 0d;
      minColor = image1.maxColor;
      maxColor = -1;
      averageColor = 0d;
      
      int occurringColors = 0;
      
      for (int colorX = 0; colorX < image1.maxColor; colorX++) {
        for (int colorY = 0; colorY < image1.maxColor; colorY++) {
          final int occurrences = data[colorX][colorY];
          
          averageOccurrences += occurrences;
          if (occurrences < minOccurringColor) {
            minOccurringColor = colorX;
          }
          
          if (occurrences > maxOccurringColor) {
            maxOccurringColor = colorX;
          }
          
          if (occurrences > 0) {
            occurringColors++;
            averageColor += colorX;
            
            if (colorX < minColor) {
              minColor = colorX;
            }
            
            if (colorX > maxColor) {
              maxColor = colorX;
            }
          }
        }
      }
      
      averageOccurrences /= (double) (image1.maxColor + 1);
      averageColor /= (double) occurringColors;
      
      calculatedStatistics = true;
    }
  }
  
}
