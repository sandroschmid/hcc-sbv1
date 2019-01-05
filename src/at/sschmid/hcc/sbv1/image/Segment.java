package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.utility.Point;
import ij.IJ;

public final class Segment implements ImageGenerator {
  
  public static Builder create(final Image image) {
    return new Builder(image);
  }
  
  public static int radius(final int size) {
    return (size - 1) / 2;
  }
  
  private final Image fullImage;
  private final Point origin;
  private final Point center;
  private final int width;
  private final int height;
  private final int xRadius;
  private final int yRadius;
  private final Image segmentImage;
  
  private Integer optimalThreshold;
  
  private Segment(final Image fullImage, final Point origin, final int width, final int height) {
    this.fullImage = fullImage;
    this.origin = origin;
    this.width = width;
    this.height = height;
    this.xRadius = radius(width);
    this.yRadius = radius(height);
    
    this.center = findCenter(fullImage, xRadius, yRadius);
    
    this.segmentImage = new Image(String.format("Segment of %s", fullImage.getName()), width, height);
    for (int xOffset = -xRadius; xOffset <= xRadius; xOffset++) {
      final int sx = xOffset + xRadius;
      final int fix = center.x + xOffset;
      for (int yOffset = -yRadius; yOffset <= yRadius; yOffset++) {
        segmentImage.data[sx][yOffset + yRadius] = fullImage.data[fix][center.y + yOffset];
      }
    }
  }
  
  @Override
  public Image getResult() {
    return segmentImage;
  }
  
  public Image mask() {
    final int left = center.x - xRadius;
    final int right = center.x + xRadius;
    final int top = center.y - yRadius;
    final int bottom = center.y + yRadius;
    
    final Image mask = new Image(fullImage.width, fullImage.height);
    for (int x = left; x < right; x++) {
      for (int y = top; y < bottom; y++) {
        mask.data[x][y] = fullImage.maxColor;
      }
    }
    
    return mask;
  }
  
  public int getOptimalThreshold() {
    if (optimalThreshold == null) {
      final OptimalThreshold optimalThreshold = segmentImage.histogram().optimalThreshold();
      IJ.log(String.format("optimal threshold for segment %s: %d", toString(), optimalThreshold.get()));
      this.optimalThreshold = optimalThreshold.get();
    }
    
    return optimalThreshold;
  }
  
  @Override
  public String toString() {
    return String.format("Segment {\n  origin=%s,\n  center=%s\n  width=%d,\n  height=%d,\n  optimalThreshold=%d\n}",
        origin,
        center,
        width,
        height,
        optimalThreshold);
  }
  
  private Point findCenter(final Image fullImage, final int xRadius, final int yRadius) {
    int centerX = origin.x;
    int centerY = origin.y;
    
    if (origin.x - xRadius < 0) {
      centerX += Math.abs(origin.x - xRadius);
    } else if (origin.x + xRadius >= fullImage.width) {
      centerX -= origin.x + xRadius - fullImage.width + 1;
    }
    
    if (origin.y - yRadius < 0) {
      centerY += Math.abs(origin.y - yRadius);
    } else if (origin.y + yRadius >= fullImage.height) {
      centerY -= origin.y + yRadius - fullImage.height + 1;
    }
    
    return new Point(centerX, centerY);
  }
  
  public static final class Builder {
  
    private final Image fullImage;
    private Point origin;
    private int width;
    private int height;
  
    public Builder(final Image fullImage) {
      this.fullImage = fullImage;
    }
  
    public Builder origin(final Point origin) {
      this.origin = origin;
      return this;
    }
    
    public Builder width(final int width) {
      if (width > fullImage.width) {
        throw new IllegalArgumentException("Segments need to be smaller than the image from which they are created");
      }
  
      if (width % 2 == 0) {
        throw new IllegalArgumentException("Width for a segment need to be an uneven number");
      }
  
      this.width = width;
      return this;
    }
  
    public Builder height(final int height) {
      if (height > fullImage.height) {
        throw new IllegalArgumentException("Segments need to be smaller than the image from which they are created");
      }
    
      if (height % 2 == 0) {
        throw new IllegalArgumentException("Height for a segment need to be an uneven number");
      }
    
      this.height = height;
      return this;
    }
    
    public Segment build() {
      return new Segment(fullImage, origin, width, height);
    }
    
  }
  
}
