package at.sschmid.hcc.sbv1.image;

import java.util.Arrays;

public final class Histogram2d {
  
  private final Image image1;
  private final Image image2;
  private final int[][] data;
  private final int count;
  
  private double[][] probabilities;
  
  public Histogram2d(final Image image1, final Image image2) {
    if (!image1.sizeEqualsTo(image2)) {
      throw new IllegalArgumentException("Images for 2d histograms must be of equal sizes");
    }
  
    if (image1.maxColor != image2.maxColor) {
      throw new IllegalArgumentException("Images for 2d histograms must have an equal `maxColor`");
    }
    
    this.image1 = image1;
    this.image2 = image2;
    this.count = image1.size;
    final int dataSize = image1.maxColor + 1;
    this.data = new int[dataSize][dataSize];
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
      final double totalPixels = (double) image1.size;
      for (int i = 0; i < probabilities.length; i++) {
        for (int j = 0; j < probabilities.length; j++) {
          probabilities[i][j] = data[i][j] / totalPixels;
        }
      }
    }
    
    return probabilities;
  }
  
  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder("2D-Histogram");
    if (image1.hasName() && image2.hasName()) {
      builder.append(" (").append(image1.getName()).append("-").append(image2.getName()).append(")");
    }
    
    return builder.append(" { count=")
        .append(count)
        .append(", data=")
        .append(Arrays.toString(data))
        .append(" }")
        .toString();
  }
  
}
