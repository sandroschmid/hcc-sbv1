package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;

final class SquaredSumOfErrorMetric implements ErrorMetric {
  
  SquaredSumOfErrorMetric() {
    // nothing to do
  }
  
  @Override
  public double getError(final Image image1, final Image image2) {
    double sseSum = 0d;
    for (int x = 0; x < image1.width; x++) {
      for (int y = 0; y < image1.height; y++) {
        final int val1 = image1.data[x][y];
        final int val2 = image2.data[x][y];
        final int diff = val1 - val2;
        sseSum += diff * diff;
      }
    }
    
    return sseSum;
  }
  
}
