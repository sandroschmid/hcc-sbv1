package at.sschmid.hcc.sbv1;

public class Checkerboard {
  
  private static final byte DEFAULT_SEGMENT_COUNT = 4;
  
  private final byte segmentCount;
  private final int[][] imgA;
  private final int[][] imgB;
  private final int width;
  private final int height;
  
  private int[][] result;
  
  public Checkerboard(final int[][] imgA, final int[][] imgB, final int width, final int height) {
    this(imgA, imgB, width, height, DEFAULT_SEGMENT_COUNT);
  }
  
  public Checkerboard(final int[][] imgA, final int[][] imgB, final int width, final int height, byte segmentCount) {
    super();
    this.imgA = imgA;
    this.imgB = imgB;
    this.width = width;
    this.height = height;
    this.segmentCount = segmentCount;
  }
  
  public Checkerboard generate() {
    final int[][] checkerboard = new int[width][height];
    
    final int segmentWidth = getSegmentCountQuotient(width);
    final int segmentHeight = getSegmentCountQuotient(height);
    
    System.out.println("Generating checkerboard with 2 images");
    System.out.println(" > Image-Size:    " + width + " x " + height);
    System.out.println(" > Segments-Size: " + segmentWidth + " x " + segmentHeight);
    
    int currentSegWidth = 0;
    int currentSegHeight = 0;
    int[][] currentImg = imgA;
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        checkerboard[x][y] = currentImg[x][y];
        if (++currentSegHeight >= segmentHeight) {
          currentSegHeight = 0;
          currentImg = currentImg == imgA ? imgB : imgA;
        }
      }
      
      if (++currentSegWidth >= segmentWidth) {
        currentSegWidth = 0;
        currentImg = currentImg == imgA ? imgB : imgA;
      }
    }
    
    result = checkerboard;
    return this;
  }
  
  public Checkerboard show() {
    if (result != null) {
      ImageJUtility.showNewImage(result, width, height, "Checkerboard");
    } else {
      throw new IllegalStateException("The checkerboard is not yet available");
    }
    
    return this;
  }
  
  private int getSegmentCountQuotient(int size) {
    while (size % segmentCount != 0) {
      size--;
    }
    
    return size / segmentCount;
  }
  
}
