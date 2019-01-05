package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.utility.Point;

public final class Segment implements ImageGenerator {
  
  public static Builder create(final Image image) {
    return new Builder(image);
  }
  
  private final Point center;
  private final int width;
  private final int height;
  private final Image segmentImage;
  
  private Integer optimalThreshold;
  
  public Segment(final Image fullImage, final Point center, final int width, final int height) {
    if (width % 2 == 0 || height % 2 == 0) {
      throw new IllegalArgumentException("Width and height for a segment need to be uneven numbers");
    }
    
    this.center = center;
    
    final int xRadius = (width - 1) / 2;
    final int yRadius = (height - 1) / 2;
    
    int actualWidth = width;
    int actualHeight = height;
    int xStart = -xRadius;
    int xEnd = xRadius;
    int yStart = -yRadius;
    int yEnd = yRadius;
    
    if (center.x - xRadius < 0) {
      final int overflow = Math.abs(center.x - xRadius);
      actualWidth -= overflow;
      xStart += overflow;
    }
    
    if (center.x + xRadius >= fullImage.width) {
      final int overflow = fullImage.width - (center.x + xRadius) + 1;
      actualWidth -= overflow;
      xEnd -= overflow;
    }
    
    if (center.y - yRadius < 0) {
      final int overflow = Math.abs(center.y - yRadius);
      actualHeight -= overflow;
      yStart += overflow;
    }
    
    if (center.y + yRadius >= fullImage.height) {
      final int overflow = fullImage.height - (center.y + yRadius) + 1;
      actualHeight -= overflow;
      yEnd -= overflow;
    }
    
    this.width = actualWidth;
    this.height = actualHeight;
    
    this.segmentImage = new Image(String.format("Segment of %s", fullImage.getName()), actualWidth, actualHeight);
    for (int xOffset = xStart; xOffset <= xEnd; xOffset++) {
      for (int yOffset = yStart; yOffset <= yEnd; yOffset++) {
        final int sx = xOffset + Math.abs(xStart);
        final int sy = yOffset + Math.abs(yStart);
        final int fix = center.x + xOffset;
        final int fiy = center.y + yOffset;
        
        if (sx >= 0
            && sx < actualWidth
            && sy >= 0
            && sy < actualHeight
            && fix >= 0
            && fix < fullImage.width
            && fiy >= 0
            && fiy < fullImage.height) {
          final int value = fullImage.data[fix][fiy];
          segmentImage.data[sx][sy] = value;
        }
      }
    }
  }
  
  @Override
  public Image getResult() {
    return segmentImage;
  }
  
  public int getOptimalThreshold() {
    if (optimalThreshold == null) {
      optimalThreshold = segmentImage.histogram().optimalThreshold().calculate();
    }
    
    return optimalThreshold;
  }
  
  @Override
  public String toString() {
    return String.format("Segment {\n  center=%s,\n  width=%d,\n  height=%d,\n  optimalThreshold=%d\n}",
        center,
        width,
        height,
        optimalThreshold);
  }
  
  public static final class Builder {
    
    private final Image image;
    private Point center;
    private int width;
    private int height;
    
    public Builder(final Image image) {
      this.image = image;
    }
    
    public Builder center(final Point position) {
      this.center = position;
      return this;
    }
    
    public Builder width(final int width) {
      this.width = width;
      return this;
    }
    
    public Builder height(final int height) {
      this.height = height;
      return this;
    }
    
    public Segment build() {
      return new Segment(image, center, width, height);
    }
    
  }
  
}
