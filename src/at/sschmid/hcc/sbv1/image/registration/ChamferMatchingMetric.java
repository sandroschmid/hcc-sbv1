package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.segmentation.BinaryThreshold;

final class ChamferMatchingMetric implements MatchMetric {
  
  private Image image1Edges;
  
  ChamferMatchingMetric() {
    // nothing to do
  }
  
  @Override
  public void init(final Image image1, final Image image2) {
    image1Edges = image1.edges().binary(new BinaryThreshold(1, 0, image1.maxColor));
  }
  
  @Override
  public double getMatch(final Image image1, final Image image2) {
    final double[][] image2Distances = image2.distanceMap(DistanceMetric.Euklid).calculate();
    double sum = 0d;
    for (int x = 0; x < image1.width; x++) {
      for (int y = 0; y < image1.height; y++) {
        if (image1Edges.data[x][y] == image1Edges.maxColor) {
          sum += image2Distances[x][y];
        }
      }
    }
  
    return sum;
  }
  
}
