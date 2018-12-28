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
    calculate();
  
    final String name = String.format("Distance map (%s) for '%s'", distanceMetric, image.getName());
    final Image result = new Image(name, image.width, image.height);
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        final double color = distanceMap[x][y];
        result.data[x][y] = color > image.maxColor ? image.maxColor : (int) (color + 0.5d);
      }
    }
  
    return result;
  }
  
  private void initContour() {
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        distanceMap[x][y] = image.data[x][y] == 255 ? 0 : Double.POSITIVE_INFINITY;
      }
    }
  }
  
  private void topLeftToBottomRight() {
    for (int x = 0; x < image.width; x++) {
      for (int y = 1; y < image.height; y++) {
        if (distanceMap[x][y] == 0) {
          continue;
        }
  
        double distance = distanceMap[x][y - 1] + distanceMetric.value[1][0]; // top
        double minDistance = distance;
        
        if (x > 0) {
          distance = distanceMap[x - 1][y] + distanceMetric.value[0][1]; // left
          if (distance < minDistance) {
            minDistance = distance;
          }
  
          distance = distanceMap[x - 1][y - 1] + distanceMetric.value[0][0]; // top left
          if (distance < minDistance) {
            minDistance = distance;
          }
        }
        
        if (x < image.width - 1) {
          distance = distanceMap[x + 1][y - 1] + distanceMetric.value[2][0]; // top right
          if (distance < minDistance) {
            minDistance = distance;
          }
        }
  
        distanceMap[x][y] = minDistance;
      }
    }
  }
  
  private void bottomRightToTopLeft() {
    for (int x = image.width - 1; x >= 0; x--) {
      for (int y = image.height - 2; y >= 0; y--) {
        double prevDistance = distanceMap[x][y];
        if (prevDistance == 0) {
          continue;
        }
  
        double minDistance = prevDistance;
        double distance = distanceMap[x][y + 1] + distanceMetric.value[1][2]; // bottom
        if (distance < minDistance) {
          minDistance = distance;
        }
        
        if (x < image.width - 1) {
          distance = distanceMap[x + 1][y] + distanceMetric.value[2][1]; // right
          if (distance < minDistance) {
            minDistance = distance;
          }
  
          distance = distanceMap[x + 1][y + 1] + distanceMetric.value[2][2]; // bottom right
          if (distance < minDistance) {
            minDistance = distance;
          }
        }
  
        if (x > 0) {
          distance = distanceMap[x - 1][y + 1] + distanceMetric.value[0][2]; // bottom left
          if (distance < minDistance) {
            minDistance = distance;
          }
        }
  
        distanceMap[x][y] = minDistance;
      }
    }
  }
  
}
