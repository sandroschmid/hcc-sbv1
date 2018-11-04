package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.utility.Point;

import java.util.logging.Logger;

public final class ImageTransformation {
  
  private static final Logger LOGGER = Logger.getLogger(ImageTransformation.class.getName());
  
  private Image image;
  
  public ImageTransformation(final Image image) {
    this.image = new Image(image);
    
    if (image.hasName()) {
      this.image.name += " (transformed)";
    } else {
      this.image.name = "Transformed";
    }
  }
  
  public ImageTransformation transfer(final int[] transferFunction) {
    final Image result = new Image(image.width, image.height);
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        result.data[x][y] = transferFunction[image.data[x][y]];
      }
    }
    
    image = result;
    return this;
  }
  
  public ImageTransformation transform(final Transformations transformations) {
    return transform(transformations, TranslationMode.BiLinear);
  }
  
  public ImageTransformation transform(final Transformations transformations, final TranslationMode translationMode) {
    while (transformations.hasNext()) {
      final Transformations.TransformationItem item = transformations.next();
      if (item instanceof Transformations.Translation) {
        translate((Transformations.Translation) item, translationMode);
      } else if (item instanceof Transformations.Rotation) {
        rotate((Transformations.Rotation) item, translationMode);
      } else {
        scale((Transformations.Scale) item, translationMode);
      }
    }
    
    return this;
  }
  
  public Image getResult() {
    return image;
  }
  
  private void translate(final Transformations.Translation translation, final TranslationMode translationMode) {
    final Image result = new Image(image.width, image.height);
    
    final double widthHalf = image.width / 2d;
    final double heightHalf = image.height / 2d;
    
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        // move coordinates to center
        double posX = x - widthHalf;
        double posY = y - heightHalf;
        
        // rotate
//        double posX = (x - widthHalf) + (y - heightHalf);
//        double posY = -(x - widthHalf) + (y - heightHalf);
        
        // translate (-= due to backward mapping)
        posX -= translation.x;
        posY -= translation.y;
        
        // move coordinates back from center to top-left corner
        posX = posX + widthHalf;
        posY = posY + heightHalf;
        
        // interpolate
        result.data[x][y] = getInterpolatedColor(posX, posY, translationMode);
      }
    }
    
    image = result;
  }
  
  private void rotate(final Transformations.Rotation rotation, final TranslationMode translationMode) {
    final Image result = new Image(image.width, image.height);
    final double cosTheta = Math.cos(rotation.radians);
    final double sinTheta = Math.sin(rotation.radians);
    
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
        
        // move coordinates back from center to top-left corner
        posX = posX + widthHalf;
        posY = posY + heightHalf;
  
        // interpolate
        result.data[x][y] = getInterpolatedColor(posX, posY, translationMode);
      }
    }
    
    image = result;
  }
  
  private void scale(final Transformations.Scale scale, final TranslationMode translationMode) {
    if (scale.factor < 0.01d || scale.factor > 10d) {
      throw new IllegalArgumentException(String.format("%f is not a valid scale. Scale must be in [0.01;10].",
          scale.factor));
    }
    
    final int newWidth = (int) (image.width * scale.factor + 0.5); // arithm round
    final int newHeight = (int) (image.height * scale.factor + 0.5);
    
    final Image result = new Image(newWidth, newHeight);
    
    // forward mapping
//		double scaleFactorX = newWidth / (double) width;
//		double scaleFactorY = newHeight / (double) height;

//    double scaleFactorX = (newWidth - 1d) / (double) width;
//    double scaleFactorY = (newWidth - 1d) / (double) height;
    double scaleFactorX = (newWidth - 1d) / (double) (image.width - 1d);
    double scaleFactorY = (newHeight - 1d) / (double) (image.height - 1d);
//    double rw = 1 / (double) (2 * width) + ((newWidth - 1d) / (double) width);
//    double rh = 1 / (double) (2 * height) + ((newHeight - 1d) / (double) height);
//    double scaleFactorX = (2 * rw * width) / 2d;
//    double scaleFactorY = (2 * rh * height) / 2d;
    
    LOGGER.info(String.format("tgtScale=%s sX=%s sY=%s", scale.factor, scaleFactorX, scaleFactorY));
    LOGGER.info(String.format("new width=%d new height=%d", newWidth, newHeight));
    
    // fill new img
    for (int x = 0; x < newWidth; x++) {
      for (int y = 0; y < newHeight; y++) {
        // calc new coordinates
        // coord in input image A utilizing backward mapping. forward mapping works only when shrinking the size
        double newX = x / scaleFactorX;
        double newY = y / scaleFactorY;
        
        // forward mapping: use x/y coords instead of newX/newY
        int interpolatedColor = getInterpolatedColor(newX, newY, translationMode);
        
        // forward mapping
//		int roundedNewX = (int)(newX + 0.5);
//		int roundedNewY = (int)(newY + 0.5);
//		if (roundedNewX >= 0 && roundedNewX < width && roundedNewY >= 0 && roundedNewY < height) {
//			scaledImg[roundedNewX][roundedNewY] = resultVal;
//		}
        
        result.data[x][y] = interpolatedColor;
      }
    }
    
    image = result;
  }
  
  private int getInterpolatedColor(final double x, final double y, final TranslationMode translationMode) {
    if (translationMode == TranslationMode.NearestNeighbour) {
      return getRawValue(new Point((int) (x + 0.5), (int) (y + 0.5)));
      
    } else {
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
  }
  
  private int getRawValue(final Point p) {
    return p.x >= 0 && p.x < image.width && p.y >= 0 && p.y < image.height ? image.data[p.x][p.y] : 0;
  }
  
  public enum TranslationMode {
    NearestNeighbour,
    BiLinear
  }
  
}
