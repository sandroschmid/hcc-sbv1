package at.sschmid.hcc.sbv1.image;

public class BinaryThreshold {
  
  private final int thresholdMin;
  private final Integer thresholdMax;
  private final int background;
  private final int foreground;
  
  public BinaryThreshold(final int thresholdMin, final int background, final int foreground) {
    this(thresholdMin, null, background, foreground);
  }
  
  public BinaryThreshold(final int thresholdMin,
                         final Integer thresholdMax,
                         final int background,
                         final int foreground) {
    this.thresholdMin = thresholdMin;
    this.thresholdMax = thresholdMax;
    this.background = background;
    this.foreground = foreground;
  }
  
  public int[] getTransformFunction(final int maxVal) {
    final int[] transferFunction = new int[maxVal + 1];
//    for (int i = 0; i <= maxVal; i++) {
//      transferFunction[i] = i >= thresholdMin && (thresholdMax == null || i <= thresholdMax) ? foreground :
//      background;
//    }
  
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
    return "BinaryThreshold {" +
        "\n  thresholdMin=" + thresholdMin +
        ",\n  thresholdMax=" + thresholdMax +
        ",\n  background=" + background +
        ",\n  foreground=" + foreground +
        "\n}";
  }
  
}
