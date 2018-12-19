package at.sschmid.hcc.sbv1.image;

import java.util.logging.Logger;

public final class Checkerboard implements ImageGenerator {
  
  private static final Logger LOGGER = Logger.getLogger(Checkerboard.class.getName());
  private static final byte DEFAULT_SEGMENT_COUNT = 4;
  
  private final byte segmentCount;
  private final String name;
  private final Image image1;
  private final Image image2;
  
  private Image result;
  
  Checkerboard(final Image image1, final Image image2) {
    this(image1, image2, DEFAULT_SEGMENT_COUNT);
  }
  
  Checkerboard(final Image image1, final Image image2, byte segmentCount) {
    super();
    this.name = new StringBuilder()
        .append(segmentCount)
        .append('x')
        .append(segmentCount)
        .append("-Checkerboard for '")
        .append(image1.getName())
        .append("' and '")
        .append(image2.getName())
        .append('\'')
        .toString();
    this.image1 = image1;
    this.image2 = image2;
    this.segmentCount = segmentCount;
    
    if (!image1.sizeEqualsTo(image2)) {
      throw new IllegalArgumentException("Checkerboard images must be equally large.");
    }
  }
  
  @Override
  public Image getResult() {
    if (result != null) {
      return result;
    }
    
    result = new Image(name, image1.width, image1.height);
    
    final int segmentWidth = getSegmentCountQuotient(result.width);
    final int segmentHeight = getSegmentCountQuotient(result.height);
  
    LOGGER.info(String.format("Generating checkerboard '%s'", name));
    LOGGER.info(String.format(" > Image-Size:    %d x %d", result.width, result.height));
    LOGGER.info(String.format(" > Segments-Size: %d x %d", segmentWidth, segmentHeight));
    
    int currentSegWidth = 0;
    int currentSegHeight = 0;
    int[][] currentImg = image1.data;
    for (int x = 0; x < result.width; x++) {
      for (int y = 0; y < result.height; y++) {
        result.data[x][y] = currentImg[x][y];
        if (++currentSegHeight >= segmentHeight) {
          currentSegHeight = 0;
          currentImg = currentImg == image1.data ? image2.data : image1.data;
        }
      }
      
      if (++currentSegWidth >= segmentWidth) {
        currentSegWidth = 0;
        currentImg = currentImg == image1.data ? image2.data : image1.data;
      }
    }
    
    return result;
  }
  
  public void show() {
    getResult().show();
  }
  
  public void show(final String label) {
    getResult().show(label);
  }
  
  @Override
  public String toString() {
    return String.format("Checkerboard { name='%s' }", name);
  }
  
  private int getSegmentCountQuotient(int size) {
    while (size % segmentCount != 0) {
      size--;
    }
    
    return size / segmentCount;
  }
  
}
