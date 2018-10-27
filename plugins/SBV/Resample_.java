import at.sschmid.hcc.sbv1.image.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.Checkerboard;
import at.sschmid.hcc.sbv1.image.ImageJUtility;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;

import java.util.logging.Logger;

public final class Resample_ extends AbstractUserInputPlugIn<Double> {
  
  private static final Logger LOGGER = Logger.getLogger(Resample_.class.getName());
  private static final double defaultScale = 4d;
  
  @Override
  public void process(final Image image) {
    if (input < 0.01d || input > 10d) {
      return;
    }
    
    final int newWidth = (int) (image.width * input + 0.5); // arithm round
    final int newHeight = (int) (image.height * input + 0.5);
    
    // forward mapping
//		double scaleFactorX = newWidth / (double) width;
//		double scaleFactorY = newHeight / (double) height;

//    double scaleFactorX = (newWidth - 1d) / (double) width;
//    double scaleFactorY = (newWidth - 1d) / (double) height;
    double scaleFactorX = (newWidth - 1d) / (double) (width - 1d);
    double scaleFactorY = (newHeight - 1d) / (double) (height - 1d);
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
        int nnVal = getNNInterpolatedValue(inDataArrInt, width,	height,	newX, newY);
        int biLinVal = getBiLinInterpolatedValue(inDataArrInt, width, height, newX, newY);
      
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
  
    new Checkerboard(scaledImgNN.data, scaledImgBiLin.data, newWidth, newHeight).generate().show();
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
    int xPos = (int) (x + 0.5);
    int yPos = (int) (y + 0.5);
    
    return getRawValue(image, xPos, yPos);
  }
  
  private int getBiLinInterpolatedValue(final Image image, double x, double y) {
    // How to get the 4 coords for e.g (3.7, 12.2)
    // P0: (3,12) P1: (4,12), P2: (3, 13), P3: (4,13)
    final int startX = (int) x; // floor
    final int startY = (int) y;

    final double xPercent = x - startX; // decimal parts
    final double yPercent = y - startY;
    
    final int interpolation1Start = getRawValue(image, startX, startY);
    final int interpolation1End = getRawValue(image, startX + 1, startY);
    final int interpolation1Diff = Math.abs(interpolation1End - interpolation1Start);
    final double interpolation1Val = interpolation1Start + xPercent * interpolation1Diff;

    final int interpolation2Start = getRawValue(image, startX, startY + 1);
    final int interpolation2End = getRawValue(image, startX + 1, startY + 1);
    final int interpolation2Diff = Math.abs(interpolation2End - interpolation2Start);
    final double interpolation2Val = interpolation2Start + xPercent * interpolation2Diff;
  
    final double interpolation3Diff = Math.abs(interpolation2Val - interpolation1Val);
    final double interpolation3Val = interpolation1Val + yPercent * interpolation3Diff;
    
    return (int) (interpolation3Val + 0.5);
  }

  private int getRawValue(final Image image, int x, int y) {
    return x >= 0 && x < image.width && y >= 0 && y < image.height ? image.data[x][y] : 0;
  }
  
}
