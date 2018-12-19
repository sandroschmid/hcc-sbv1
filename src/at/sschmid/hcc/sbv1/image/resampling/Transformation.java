package at.sschmid.hcc.sbv1.image.resampling;

import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.ImageGenerator;

import java.util.logging.Logger;

public final class Transformation implements ImageGenerator {
  
  private static final Logger LOGGER = Logger.getLogger(Transformation.class.getName());
  
  private Image image;
  
  public Transformation(final Image image) {
    this(image, image.hasName() ? image.name + " (transformed)" : "Transformed");
  }
  
  public Transformation(final Image image, final String name) {
    this.image = new Image(image).withName(name);
  }
  
  public Image invert() {
    final Image result = new Image(image, false);
    for (int x = 0; x < result.width; x++) {
      for (int y = 0; y < result.height; y++) {
        result.data[x][y] = image.maxColor - image.data[x][y];
      }
    }
    return result;
  }
  
  public Transformation transfer(final int[] transferFunction) {
    final Image result = new Image(image, false);
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        result.data[x][y] = transferFunction[image.data[x][y]];
      }
    }
    
    image = result;
    return this;
  }
  
  public Transformation transform(final Transformations transformations) {
    return transform(transformations, Interpolation.Mode.BiLinear);
  }
  
  public Transformation transform(final Transformations transformations,
                                  final Interpolation.Mode mode) {
    while (transformations.hasNext()) {
      final Transformations.TransformationItem item = transformations.next();
      if (item instanceof Transformations.Translation) {
        translate((Transformations.Translation) item, mode);
      } else if (item instanceof Transformations.Rotation) {
        rotate((Transformations.Rotation) item, mode);
      } else {
        scale((Transformations.Scale) item, mode);
      }
    }
    
    return this;
  }
  
  @Override
  public Image getResult() {
    return image;
  }
  
  private void translate(final Transformations.Translation translation,
                         final Interpolation.Mode mode) {
    final Image result = new Image(image, false);
    final Interpolation interpolation = image.interpolation();
    
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
        result.data[x][y] = interpolation.getColor(posX, posY, mode);
      }
    }
    
    image = result;
  }
  
  private void rotate(final Transformations.Rotation rotation, final Interpolation.Mode mode) {
    final Image result = new Image(image, false);
    final Interpolation interpolation = image.interpolation();
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
        result.data[x][y] = interpolation.getColor(posX, posY, mode);
      }
    }
    
    image = result;
  }
  
  private void scale(final Transformations.Scale scale, final Interpolation.Mode mode) {
    if (scale.factor < 0.01d || scale.factor > 10d) {
      throw new IllegalArgumentException(String.format("%f is not a valid scale. Scale must be in [0.01;10].",
          scale.factor));
    }
    
    final int newWidth = (int) (image.width * scale.factor + 0.5); // arithm round
    final int newHeight = (int) (image.height * scale.factor + 0.5);
    
    final Image result = new Image(image.name, newWidth, newHeight);
    final Interpolation interpolation = image.interpolation();
    
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
        int interpolatedColor = interpolation.getColor(newX, newY, mode);
        
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
  
}
