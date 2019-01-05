package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.image.segmentation.BinaryThreshold;

import java.util.List;

public final class OptimalThreshold {
  
  private final Image image;
  
  private Integer globalThreshold;
  
  OptimalThreshold(final Image image) {
    this.image = image;
  }
  
  public int get() {
    if (globalThreshold == null) {
      double threshold = image.maxColor / 2.0d;
  
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
  
      globalThreshold = (int) (threshold + 0.5d);
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
