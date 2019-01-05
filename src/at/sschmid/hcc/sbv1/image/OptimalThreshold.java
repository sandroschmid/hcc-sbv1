package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.image.segmentation.BinaryThreshold;
import at.sschmid.hcc.sbv1.utility.Point;

public final class OptimalThreshold {
  
  private final Image image;
  private final Histogram histogram;
  
  private Integer globalThreshold;
  
  public OptimalThreshold(final Histogram histogram) {
    this.image = histogram.getImage();
    this.histogram = histogram;
  }
  
  public int calculate() {
    if (globalThreshold == null) {
      double prevThreshold = histogram.getAverageColor();
      
      Image objectsMask = image.binary(new BinaryThreshold((int) prevThreshold + 1, 0, image.maxColor));
      Image objects = image.calculation(objectsMask).and();
      
      double threshold = (prevThreshold + objects.histogram().getAverageColor()) / 2.0d;
      double prevDiff = -1;
      double diff = Math.abs(prevThreshold - threshold);
      while (prevDiff != diff) {
        objectsMask = image.binary(new BinaryThreshold((int) prevThreshold + 1, 0, image.maxColor));
        objects = image.calculation(objectsMask).and();
        
        threshold = (prevThreshold + objects.histogram().getAverageColor()) / 2.0d;
        prevDiff = diff;
        diff = Math.abs(prevThreshold - threshold);
      }
      
      globalThreshold = (int) (threshold + 0.5d);
    }
    
    return globalThreshold;
  }
  
  public Segment[][] calculate(final int segmentWidth, final int segmentHeight) {
    final Segment[][] segments = new Segment[image.width][image.height];
    final Segment.Builder segmentBuilder = Segment.create(image).width(segmentWidth).height(segmentHeight);
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        segments[x][y] = segmentBuilder.center(new Point(x, y)).build();
      }
    }
    
    return segments;
  }
  
}
