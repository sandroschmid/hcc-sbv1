package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.utility.Point;

public final class ImageTransform {
  
  private Image image;
  
  public ImageTransform(final Image image) {
    this.image = new Image(image);
    
    if (image.hasName()) {
      this.image.name += " (transformed)";
    } else {
      this.image.name = "Transformed";
    }
  }
  
  public ImageTransform transfer(final int[] transferFunction) {
    final Image result = new Image(image.width, image.height);
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        result.data[x][y] = transferFunction[image.data[x][y]];
      }
    }
  
    image = result;
    return this;
  }
  
  public ImageTransform transform(final Transformation transformation) {
    final Image result = new Image(image.width, image.height);
    final double cosTheta = Math.cos(Math.toRadians(transformation.rotation));
    final double sinTheta = Math.sin(Math.toRadians(transformation.rotation));
  
    final double widthHalf = image.width / 2d;
    final double heightHalf = image.height / 2d;
  
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        // move coordinates to center
//				double posX = x - widthHalf;
//				double posY = y - heightHalf;
        
        // rotate
        double posX = (x - widthHalf) * cosTheta + (y - heightHalf) * sinTheta;
        double posY = -(x - widthHalf) * sinTheta + (y - heightHalf) * cosTheta;
        
        // translate (-= due to backward mapping)
        posX -= transformation.translationX;
        posY -= transformation.translationY;
        
        // move coordinates back from center to top-left corner
        posX = posX + widthHalf;
        posY = posY + heightHalf;
        
        // interpolate (TODO use bilinear)
        int nnX = (int) (posX + .5d);
        int nnY = (int) (posY + .5d);
      
        if (nnX >= 0 && nnX < image.width && nnY >= 0 && nnY < image.height) {
          result.data[x][y] = image.data[nnX][nnY];
        }

//        result.data[x][y] = getNNInterpolatedValue(posX, posY);
//        result.data[x][y] = getBiLinInterpolatedValue(posX, posY);
      }
    }
  
    image = result;
    return this;
  }
  
  public Image getResult() {
    return image;
  }
  
  private int getNNInterpolatedValue(double x, double y) {
    return getRawValue(new Point((int) (x + 0.5), (int) (y + 0.5)));
  }
  
  private int getBiLinInterpolatedValue(double x, double y) {
    // How to get the 4 coords for e.g (3.7, 12.2)
    // P0: (3,12) P1: (4,12), P2: (3, 13), P3: (4,13)
    final Point p1 = new Point((int) x, (int) y);
    final Point p2 = new Point(p1.x, p1.y + 1);
    final Point p3 = new Point(p1.x + 1, p1.y);
    final Point p4 = new Point(p1.x + 1, p1.y + 1);
    
    final double xPercentage = x - p1.x;
    final double yPercentage = y - p1.y;
    
    final int p1Color = getRawValue(p1);
    final int p2Color = getRawValue(p2);
    final int p3Color = getRawValue(p3);
    final int p4Color = getRawValue(p4);
    
    final double interpolatedColor1 = p1Color + xPercentage * (p2Color - p1Color);
    final double interpolatedColor2 = p3Color + xPercentage * (p3Color - p4Color);
    final double interpolatedColor3 = interpolatedColor1 + yPercentage * (interpolatedColor2 - interpolatedColor1);
    
    return (int) interpolatedColor3;
  }
  
  private int getRawValue(final Point p) {
    return p.x >= 0 && p.x < image.width && p.y >= 0 && p.y < image.height ? image.data[p.x][p.y] : 0;
  }
  
}
