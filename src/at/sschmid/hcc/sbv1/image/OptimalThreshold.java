package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.image.segmentation.BinaryThreshold;
import ij.IJ;

import java.util.List;

public final class OptimalThreshold {
  
  private final Image image;
  private final Histogram histogram;
  
  private Integer globalThreshold;
  
  OptimalThreshold(final Histogram histogram) {
    this.image = histogram.getImage();
    this.histogram = histogram;
  }
  
  public int get() {
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
        IJ.log(String.format("prevT=%.2f, t=%.2f, diff=%.2f", prevThreshold, threshold, diff));
      }
  
      globalThreshold = (int) (prevThreshold + 0.5d);
    }
    
    return globalThreshold;
  }
  
  public Image globalMask() {
    return image.binary(new BinaryThreshold(get(), 0, image.maxColor));
  }
  
  public Image global() {
    return image.calculation(globalMask()).and();
  }
  
  public Image localMask(final int segmentWidth, final int segmentHeight) {
    final List<Segment> segments = image.getSegments(segmentWidth, segmentHeight);
    Image mask = new Image(image.width, image.height);
    for (final Segment segment : segments) {
      final int optimalThreshold = segment.getOptimalThreshold();
      final BinaryThreshold binaryThreshold = new BinaryThreshold(optimalThreshold, 0, image.maxColor);
      final Image sectionInImage = image.calculation(segment.mask()).and();
      mask = mask.calculation(sectionInImage.binary(binaryThreshold)).or();
    }
    
    return mask;
  }
  
  public Image local(final int segmentWidth, final int segmentHeight) {
    return image.calculation(localMask(segmentWidth, segmentHeight)).and();
  }
  
}
