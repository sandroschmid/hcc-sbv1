package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;

public final class DistanceMap {
  
  private final Image image;
  private final DistanceMetric distanceMetric;
  
  private double[][] distanceMap;
  
  public DistanceMap(final Image image, final DistanceMetric distanceMetric) {
    this.image = image;
    this.distanceMetric = distanceMetric;
  }
  
  public double[][] calculate() {
    if (distanceMap == null) {
      distanceMap = new double[image.width][image.height];
      
      initContour();
      topLeftToBottomRight();
      bottomRightToTopLeft();
    }
    
    return distanceMap;
  }
  
  public Image asImage() {
    return new Image(calculate(), image.width, image.height)
        .withName(String.format("Distance map (%s) for '%s'", distanceMetric, image.getName()));
  }
  
  private void initContour() {
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        distanceMap[x][y] = image.data[x][y] == 255 ? 0 : Double.POSITIVE_INFINITY;
      }
    }
  }
  
  private void topLeftToBottomRight() {
    final double[] distances = new double[4];
    byte i;
    for (int x = 0; x < image.width; x++) {
      for (int y = 1; y < image.height; y++) {
        if (Double.isFinite(distanceMap[x][y])) {
          continue;
        }
  
        i = 0x0;
        distances[i++] = distanceMap[x][y - 1] + distanceMetric.value[1][0]; // top
        
        if (x > 0) {
          distances[i++] = distanceMap[x - 1][y] + distanceMetric.value[0][1]; // left
          distances[i++] = distanceMap[x - 1][y - 1] + distanceMetric.value[0][0]; // top left
        }
        
        if (x < image.width - 1) {
          distances[i++] = distanceMap[x + 1][y - 1] + distanceMetric.value[2][0]; // top right
        }
        
        distanceMap[x][y] = min(distances, i);
      }
    }
  }
  
  private void bottomRightToTopLeft() {
    final double[] distances = new double[4];
    byte i;
    for (int x = image.width - 1; x >= 0; x--) {
      for (int y = image.height - 2; y >= 0; y--) {
        if (Double.isFinite(distanceMap[x][y])) {
          continue;
        }
  
        i = 0x0;
        distances[i++] = distanceMap[x][y + 1] + distanceMetric.value[1][2]; // bottom
        
        if (x < image.width - 2) {
          distances[i++] = distanceMap[x + 1][y] + distanceMetric.value[2][1]; // right
          distances[i++] = distanceMap[x + 1][y + 1] + distanceMetric.value[2][2]; // bottom right
        }
        
        if (x >= 1) {
          distances[i++] = distanceMap[x - 1][y + 1] + distanceMetric.value[0][2]; // bottom left
        }
        
        distanceMap[x][y] = min(distances, i);
      }
    }
  }
  
  private double min(final double[] distances, final byte n) {
    double minDistance = Double.POSITIVE_INFINITY;
    for (byte i = 0x0; i < n; i++) {
      final double distance = distances[i];
      if (distance < minDistance) {
        minDistance = distance;
      }
    }
    
    return minDistance;
  }
  
  public enum DistanceMetric {
    Manhattan(new double[][] {
        { 2, 1, 2 },
        { 1, 0, 1 },
        { 2, 1, 2 }
    }),
    Euklid(new double[][] {
        { 1.41, 1, 1.41 },
        { 1, 0, 1 },
        { 1.41, 1, 1.41 }
    });
  
    private final double[][] value;
  
    DistanceMetric(final double[][] value) {
      this.value = value;
    }
  }
  
}
