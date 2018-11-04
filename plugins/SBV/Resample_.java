import at.sschmid.hcc.sbv1.image.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.utility.Point;
import ij.gui.GenericDialog;

import java.util.logging.Logger;

public final class Resample_ extends AbstractUserInputPlugIn<Double> {
  
  private static final Logger LOGGER = Logger.getLogger(Resample_.class.getName());
  private static final double defaultScale = 4d;
  
  @Override
  public void process(final Image image) {
    if (input < 0.01d || input > 10d) {
      LOGGER.info(String.format("%f is not a valid scale. Scale must be in [0.01;10].", input));
      return;
    }
    
    final int newWidth = (int) (image.width * input + 0.5); // arithm round
    final int newHeight = (int) (image.height * input + 0.5);
    
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
    
    LOGGER.info(String.format("tgtScale=%s sX=%s sY=%s", input, scaleFactorX, scaleFactorY));
    LOGGER.info(String.format("new width=%d new height=%d", newWidth, newHeight));
    
    // fill new img
    final Image scaledImgNN = new Image(newWidth, newHeight);
    final Image scaledImgBiLin = new Image(newWidth, newHeight);
    final Image scaledImgDiff = new Image(newWidth, newHeight);
    for (int x = 0; x < newWidth; x++) {
      for (int y = 0; y < newHeight; y++) {
        // calc new coordinates
        // coord in input image A utilizing backward mapping. forward mapping works only when shrinking the size
        double newX = x / scaleFactorX;
        double newY = y / scaleFactorY;
        
        // forward mapping: use x/y coords instead of newX/newY
        int nnVal = getNNInterpolatedValue(image,	newX, newY);
        int biLinVal = getBiLinInterpolatedValue(image, newX, newY);
        
        // forward mapping
//		int roundedNewX = (int)(newX + 0.5);
//		int roundedNewY = (int)(newY + 0.5);
//		if (roundedNewX >= 0 && roundedNewX < width && roundedNewY >= 0 && roundedNewY < height) {
//			scaledImg[roundedNewX][roundedNewY] = resultVal;
//		}
        
        scaledImgNN.data[x][y] = nnVal;
        scaledImgBiLin.data[x][y] = biLinVal;
        scaledImgDiff.data[x][y] = Math.abs(nnVal - biLinVal);
      }
    }
    
    addResult(scaledImgNN, String.format("%s - NN", pluginName));
    addResult(scaledImgBiLin, String.format("%s - Bi-Linear", pluginName));
    addResult(scaledImgDiff, String.format("%s - Diff", pluginName));
    addResult(scaledImgNN.checkerboard(scaledImgBiLin));
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Scale", defaultScale, 2);
  }
  
  @Override
  protected Double getInput(final GenericDialog dialog) {
    return dialog.getNextNumber();
  }
  
  private int getNNInterpolatedValue(final Image image, final double x, final double y) {
    return getRawValue(image, new Point((int) (x + 0.5), (int) (y + 0.5)));
  }
  
  private int getBiLinInterpolatedValue(final Image image, double x, double y) {
    // How to get the 4 coords for e.g (3.7, 12.2)
    // P0: (3;12) P1: (4;12), P2: (3;13), P3: (4;13)
    final Point p0 = new Point((int) x, (int) y);
    final Point p1 = new Point(p0.x, p0.y + 1);
    final Point p2 = new Point(p0.x + 1, p0.y);
    final Point p3 = new Point(p0.x + 1, p0.y + 1);
    
    final double xPercentage = x - p0.x;
    final double yPercentage = y - p0.y;
    
    final int p0Color = getRawValue(image, p0);
    final int p1Color = getRawValue(image, p1);
    final int p2Color = getRawValue(image, p2);
    final int p3Color = getRawValue(image, p3);
    
    final double interpolatedColor1 = p0Color + xPercentage * (p1Color - p0Color);
    final double interpolatedColor2 = p2Color + xPercentage * (p3Color - p2Color);
    final double interpolatedColor3 = interpolatedColor1 + yPercentage * (interpolatedColor2 - interpolatedColor1);
    
    return (int) interpolatedColor3;
  }
  
  private int getRawValue(final Image image, final Point p) {
    return p.x >= 0 && p.x < image.width && p.y >= 0 && p.y < image.height ? image.data[p.x][p.y] : 0;
  }
  
}
