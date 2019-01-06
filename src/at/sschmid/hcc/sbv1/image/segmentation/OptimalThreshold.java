package at.sschmid.hcc.sbv1.image.segmentation;

import at.sschmid.hcc.sbv1.image.Histogram;
import at.sschmid.hcc.sbv1.image.Image;

import java.util.List;

public final class OptimalThreshold {
  
  private final Image image;
  private final Histogram histogram;
  private final int version;
  
  private Integer globalThreshold_v1;
  private Integer globalThreshold_v2;
  
  public OptimalThreshold(final Histogram histogram, final int version) {
    if (version < 1 || version > 2) {
      throw new IllegalArgumentException("Unknown version");
    }
    
    this.image = histogram.getImage();
    this.histogram = histogram;
    this.version = version;
  }
  
  public int globalValue() {
    return version == 1 ? v1() : v2();
  }
  
  public Image globalMask() {
    return image.binary(new BinaryThreshold(globalValue(), 0, image.maxColor));
  }
  
  public Image global() {
    return image.calculation(globalMask()).and();
  }
  
  public Image localMask(final int segmentWidth, final int segmentHeight) {
    final List<Segment> segments = image.getSegments(segmentWidth, segmentHeight);
    Image mask = new Image(image.width, image.height);
    for (final Segment segment : segments) {
      final int optimalThreshold = segment.getSegmentImage().histogram().optimalThreshold(version).globalValue();
      final BinaryThreshold binaryThreshold = new BinaryThreshold(optimalThreshold, 0, image.maxColor);
      final Image sectionInImage = image.calculation(segment.mask()).and();
      mask = mask.calculation(sectionInImage.binary(binaryThreshold)).or();
    }
    
    return mask;
  }
  
  public Image local(final int segmentWidth, final int segmentHeight) {
    return image.calculation(localMask(segmentWidth, segmentHeight)).and();
  }
  
  private int v1() {
    if (globalThreshold_v1 == null) {
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
      
      globalThreshold_v1 = (int) (prevThreshold + 0.5d);
    }
    
    return globalThreshold_v1;
  }
  
  private int v2() {
    if (globalThreshold_v2 == null) {
      double threshold = histogram.getAverageColor();
      
      double bgSum = 0d;
      int bgCount = 0;
      double bgAverage;
      
      double fgSum = 0d;
      int fgCount = 0;
      double fgAverage;
      
      double tempThreshold;
      
      do {
        for (int x = 0; x < image.width; x++) {
          for (int y = 0; y < image.height; y++) {
            final int color = image.data[x][y];
            if (color < threshold) {
              bgSum += color;
              bgCount++;
            } else {
              fgSum += color;
              fgCount++;
            }
          }
        }
        
        bgAverage = bgCount > 0 ? bgSum / bgCount : 0;
        fgAverage = fgCount > 0 ? fgSum / fgCount : 0;
        
        tempThreshold = (bgAverage + fgAverage) / 2.0d;
        if (threshold != tempThreshold) {
          threshold = tempThreshold;
          bgSum = fgSum = 0d;
          bgCount = fgCount = 0;
        } else {
          break;
        }
      } while (true);
      
      globalThreshold_v2 = (int) (threshold + 0.5d);
    }
    
    return globalThreshold_v2;
  }
  
}
