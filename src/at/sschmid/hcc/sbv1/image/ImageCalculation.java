package at.sschmid.hcc.sbv1.image;

public final class ImageCalculation {
  
  private final Image image1;
  private final Image image2;
  
  ImageCalculation(final Image image1, final Image image2) {
    this.image1 = image1;
    this.image2 = image2;
    
    if (!image1.sizeEqualsTo(image2)) {
      throw new IllegalArgumentException("Cannot perform calculations on images with different dimensions.");
    }
  }
  
  public Image difference() {
    return calculate("Difference", (c1, c2) -> Math.abs(c1 - c2));
  }
  
  public Image and() {
    return calculate("AND", (c1, c2) -> c2 > 0 ? c1 : 0);
  }
  
  private Image calculate(final String operationName, final Operation operation) {
    final Image result = new Image(getImageName(operationName), image1.width, image1.height);
    for (int x = 0; x < image1.width; x++) {
      for (int y = 0; y < image1.height; y++) {
        result.data[x][y] = operation.getResultColor(image1.data[x][y], image2.data[x][y]);
      }
    }
    
    return result;
  }
  
  private String getImageName(final String operation) {
    return String.format("%s of '%s' and '%s'", operation, image1.getName("Image 1"), image2.getName("Image 2"));
  }
  
  @FunctionalInterface
  private interface Operation {
    
    int getResultColor(final int color1, final int color2);
    
  }
  
}
