package at.sschmid.hcc.sbv1.image;

public final class DistanceMap {
  
  private static final double[][] MANHATTAN = {
      { 2, 1, 2 },
      { 1, 0, 1 },
      { 2, 1, 2 }
  };
  
  private static final double[][] EUKLID = {
      { 1.41, 1, 1.41 },
      { 1, 0, 1 },
      { 1.41, 1, 1.41 }
  };
  
  private final Image image;
  
  private double[][] distanceMetric;
  private double[][] distanceMap;
  
  public DistanceMap(final Image image, final DistanceMetric distanceMetric) {
    this.image = image;
    this.distanceMetric = distanceMetric.equals(DistanceMetric.Manhattan) ? MANHATTAN : EUKLID;
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
  
  private void initContour() {
    final Image edges = image.edges();
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        distanceMap[x][y] = edges.data[x][y] == 255 ? 0 : Double.POSITIVE_INFINITY;
      }
    }
  }
  
  private void topLeftToBottomRight() {
    final double[] distances = new double[4];
    for (int x = 0; x < image.width; x++) {
      for (int y = 1; y < image.height; y++) {
        if (Double.isFinite(distanceMap[x][y])) {
          continue;
        }
        
        byte i = 0x0;
        distances[i++] = distanceMap[x][y - 1] + distanceMetric[1][0]; // top
        
        if (x > 0) {
          distances[i++] = distanceMap[x - 1][y] + distanceMetric[0][1]; // left
          distances[i++] = distanceMap[x - 1][y - 1] + distanceMetric[0][0]; // top left
        }
        
        if (x < image.width - 1) {
          distances[i++] = distanceMap[x + 1][y - 1] + distanceMetric[2][0]; // top right
        }
        
        distanceMap[x][y] = min(distances, i);
      }
    }
  }
  
  private void bottomRightToTopLeft() {
    final double[] distances = new double[4];
    for (int x = image.width - 1; x >= 0; x--) {
      for (int y = image.height - 2; y >= 0; y--) {
        if (Double.isFinite(distanceMap[x][y])) {
          continue;
        }
        
        byte i = 0x0;
        distances[i++] = distanceMap[x][y + 1] + distanceMetric[1][2]; // bottom
        
        if (x < image.width - 2) {
          distances[i++] = distanceMap[x + 1][y] + distanceMetric[2][1]; // right
          distances[i++] = distanceMap[x + 1][y + 1] + distanceMetric[2][2]; // bottom right
        }
        
        if (x >= 1) {
          distances[i++] = distanceMap[x - 1][y + 1] + distanceMetric[0][2]; // bottom left
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
    Manhattan,
    Euklid
  }
  
}
