package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.segmentation.BinaryThreshold;
import at.sschmid.hcc.sbv1.utility.Point;

import java.util.Collection;
import java.util.LinkedList;

final class ChamferMatchingMetric implements MatchMetric {
  
  private Collection<Point> image1EdgePoints;
  
  ChamferMatchingMetric() {
    // nothing to do
  }
  
  @Override
  public void init(final Image image1, final Image image2) {
    final Image image1Edges = image1.edges().binary(new BinaryThreshold(1, 0, image1.maxColor));
    image1EdgePoints = new LinkedList<>();
    for (int x = 0; x < image1.width; x++) {
      for (int y = 0; y < image1.height; y++) {
        if (image1Edges.data[x][y] == image1Edges.maxColor) {
          image1EdgePoints.add(new Point(x, y));
        }
      }
    }
  }
  
  @Override
  public double getMatch(final Image image1, final Image image2) {
    final double[][] image2Distances = image2.distanceMap(DistanceMetric.Euklid).calculate();
    double sum = 0d;
    for (final Point point : image1EdgePoints) {
      sum += image2Distances[point.x][point.y];
    }
  
    return sum;
  }
  
}
