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
      double threshold = (int) (image.maxColor / 2.0d + 0.5d);
  
      double sumBg;
      int countBg;
      double meanBg;
  
      double sumFg;
      int countFg;
      double meanFg;
  
      double tempThreshold;
  
      do {
        sumBg = sumFg = 0d;
        countBg = countFg = 0;
    
        for (int x = 0; x < image.width; x++) {
          for (int y = 0; y < image.height; y++) {
            final int color = image.data[x][y];
            if (color < threshold) {
              sumBg += color;
              countBg++;
            } else {
              sumFg += color;
              countFg++;
            }
          }
        }
    
        meanBg = countBg > 0 ? sumBg / countBg : 0;
        meanFg = countFg > 0 ? sumFg / countFg : 0;
    
        tempThreshold = (meanBg + meanFg) / 2.0d;
        if (threshold != tempThreshold) {
          threshold = tempThreshold;
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
