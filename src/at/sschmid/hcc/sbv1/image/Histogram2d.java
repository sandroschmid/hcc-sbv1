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
        if (image1.data[x][y] > image1.maxColor) {
          System.out.println(String.format("image1[%d,%d]=%d", x, y, image1.data[x][y]));
        }
        if (image2.data[x][y] > image1.maxColor) {
          System.out.println(String.format("image2[%d,%d]=%d", x, y, image2.data[x][y]));
        }
        this.data[image1.data[x][y]][image2.data[x][y]]++;
      }
    }

//    for (int x = 0; x < dataSize; x++) {
//      for (int y = 0; y < dataSize; y++) {
//        final int occurrences = data[x][y];
//        if (occurrences > 0) {
//          System.out.println(String.format("data[%d,%d]=%d", x, y, occurrences));
//        }
//      }
//    }
  }
  
  public int[][] getData() {
    return data;
  }
  
  public Image asImage() {
    final String name = String.format("2D-Histogram for '%s' and '%s'", image1.getName(), image2.getName());
    final Image result = new Image(name, data.length, data.length);
    for (int x = 0; x < data.length; x++) {
      for (int y = 0; y < data.length; y++) {
        final int occurrences = data[x][y];
        result.data[x][y] = occurrences > image1.maxColor ? image1.maxColor : occurrences;
      }
    }
    
    return result;
  }
  
  public double[][] getProbabilities() {
    if (probabilities == null) {
      probabilities = new double[data.length][data.length];
      final double totalPixels = (double) image1.size;
//      final double totalPixels = (double) image1.size * image2.size;
//      final double totalPixels = (double)  probabilities.length * probabilities.length
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
