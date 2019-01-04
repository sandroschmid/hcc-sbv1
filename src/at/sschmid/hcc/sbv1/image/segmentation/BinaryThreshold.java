package at.sschmid.hcc.sbv1.image.segmentation;

public final class BinaryThreshold {
  
  public final int thresholdMin;
  public final Integer thresholdMax;
  public final int background;
  public final int foreground;
  
  public BinaryThreshold(final int thresholdMin, final int background, final int foreground) {
    this(thresholdMin, null, background, foreground);
  }
  
  public BinaryThreshold(final int thresholdMin,
                         final Integer thresholdMax,
                         final int background,
                         final int foreground) {
    this.thresholdMin = thresholdMin;
    this.thresholdMax = thresholdMax == null || thresholdMax <= thresholdMin ? null : thresholdMax;
    this.background = background;
    this.foreground = foreground;
  }
  
  public int[] getTransformFunction(final int maxVal) {
    final int[] transferFunction = new int[maxVal + 1];
    final int foregroundStart = Math.min(thresholdMin, maxVal);
    int i = 0;
    for (; i < foregroundStart; i++) {
      transferFunction[i] = background;
    }
    
    final int foregroundEnd = Math.min((thresholdMax == null ? maxVal : thresholdMax), maxVal);
    for (; i <= foregroundEnd; i++) {
      transferFunction[i] = foreground;
    }
    
    for (; i <= maxVal; i++) {
      transferFunction[i] = background;
    }
    
    return transferFunction;
  }
  
  @Override
  public String toString() {
    return String.format(
        "BinaryThreshold {\n  thresholdMin=%d,\n  thresholdMax=%d,\n  background=%d,\n  foreground=%d\n}",
        thresholdMin,
        thresholdMax,
        background,
        foreground);
  }
  
}
