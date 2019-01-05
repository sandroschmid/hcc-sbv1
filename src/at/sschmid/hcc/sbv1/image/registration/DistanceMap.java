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
  
      // see also https://github.com/biometrics/imagingbook/blob/master/src/Chamfer_Matching.java
      initContour();
      topLeftToBottomRight();
      bottomRightToTopLeft();
//      cleanup();
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
      for (int y = 0; y < image.height; y++) {
        double minDistance = distanceMap[x][y];
        if (minDistance == 0) {
          continue;
        }
  
        double nextDistance;
        if (x > 0) {
          nextDistance = distanceMap[x - 1][y] + distanceMetric.direct; // left
          if (nextDistance < minDistance) {
            minDistance = nextDistance;
          }
        }
  
        if (y > 0) {
          nextDistance = distanceMap[x][y - 1] + distanceMetric.direct; // top
          if (nextDistance < minDistance) {
            minDistance = nextDistance;
          }
    
          if (x > 0) {
            nextDistance = distanceMap[x - 1][y - 1] + distanceMetric.diagonal; // top left
            if (nextDistance < minDistance) {
              minDistance = nextDistance;
            }
          }
    
          if (x < image.width - 1) {
            nextDistance = distanceMap[x + 1][y - 1] + distanceMetric.diagonal; // top right
            if (nextDistance < minDistance) {
              minDistance = nextDistance;
            }
          }
        }
  
        distanceMap[x][y] = minDistance;
      }
    }
  }
  
  private void bottomRightToTopLeft() {
    for (int x = image.width - 1; x >= 0; x--) {
      for (int y = image.height - 1; y >= 0; y--) {
        double minDistance = distanceMap[x][y];
        if (minDistance == 0) {
          continue;
        }
  
        double nextDistance;
        if (x < image.width - 1) {
          nextDistance = distanceMap[x + 1][y] + distanceMetric.direct; // right
          if (nextDistance < minDistance) {
            minDistance = nextDistance;
          }
        }
  
        if (y < image.height - 1) {
          nextDistance = distanceMap[x][y + 1] + distanceMetric.direct; // bottom
          if (nextDistance < minDistance) {
            minDistance = nextDistance;
          }
    
          if (x < image.width - 1) {
            nextDistance = distanceMap[x + 1][y + 1] + distanceMetric.diagonal; // bottom right
            if (nextDistance < minDistance) {
              minDistance = nextDistance;
            }
          }
    
          if (x > 0) {
            nextDistance = distanceMap[x - 1][y + 1] + distanceMetric.diagonal; // bottom left
            if (nextDistance < minDistance) {
              minDistance = nextDistance;
            }
          }
        }
  
        distanceMap[x][y] = minDistance;
      }
    }
  }

//  private void cleanup() {
//    Double maxDistance = null;
//    for (int x = 0; x < image.width; x++) {
//      for (int y = 0; y < image.height; y++) {
//        if (Double.isInfinite(distanceMap[x][y])) {
//          if (maxDistance == null) {
//            maxDistance = maxDistance();
//          }
//
//          distanceMap[x][y] = maxDistance;
//        }
//      }
//    }
//  }
//
//  private double maxDistance() {
//    double maxDistance = 0;
//    for (int x = 0; x < image.width; x++) {
//      for (int y = 0; y < image.height; y++) {
//        final double distance = distanceMap[x][y];
//        if (distance > maxDistance) {
//          maxDistance = distance;
//        }
//      }
//    }
//
//    return maxDistance;
//  }
  
}
