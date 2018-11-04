package at.sschmid.hcc.sbv1.image;

public final class ImageCalculation {
  
  private final Image image1;
  private final Image image2;
  
  public ImageCalculation(final Image image1, final Image image2) {
    this.image1 = image1;
    this.image2 = image2;
    
    if (!image1.sizeEqualsTo(image2)) {
      throw new IllegalArgumentException("Cannot perform calculations on images with different dimensions.");
    }
  }
  
  public Image difference() {
    return calculate((c1, c2) -> Math.abs(c1 - c2));
  }
  
  private Image calculate(final Operation operation) {
    final Image result = new Image(image1.width, image1.height);
    for (int x = 0; x < image1.width; x++) {
      for (int y = 0; y < image1.height; y++) {
        result.data[x][y] = operation.getResultColor(image1.data[x][y], image2.data[x][y]);
      }
    }
    
    return result;
  }
  
  @FunctionalInterface
  private interface Operation {
    
    int getResultColor(final int color1, final int color2);
    
  }
  
}
