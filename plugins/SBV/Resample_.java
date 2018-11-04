import at.sschmid.hcc.sbv1.image.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.Image;
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
    
    double scaleFactorX = (newWidth - 1.0) / (double) image.width;
    double scaleFactorY = (newHeight - 1.0) / (double) image.height;
  
    LOGGER.info(String.format("tgtScale=%s sX=%s sY=%s", input, scaleFactorX, scaleFactorY));
    LOGGER.info(String.format("new width=%d new height=%d", newWidth, newHeight));
    
    // fill new img
    final Image resultImg = new Image(newWidth, newHeight);
    for (int x = 0; x < newWidth; x++) {
      for (int y = 0; y < newHeight; y++) {
        // calc new coordinates// coord in input image A utilizing backward mapping. forward mapping works only when
        // shrinking the size
        final double newX = x / scaleFactorX;
        final double newY = y / scaleFactorY;
        
        // forward mapping: use x/y coords instead of newX/newY
        final int resultVal = getNNInterpolatedValue(image, newX, newY);
//        final int resultVal = getBiLinInterpolatedValue(image, newX, newY);
        
        // forward mapping
//				int roundedNewX = (int)(newX + 0.5);
//				int roundedNewY = (int)(newY + 0.5);
//				if (roundedNewX >= 0 && roundedNewX < width && roundedNewY >= 0 && roundedNewY < height) {
//					scaledImg[roundedNewX][roundedNewY] = resultVal;
//				}
        
        resultImg.data[x][y] = resultVal;
      }
    }
    
    addResult(resultImg, pluginName);
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
    
    if (xPos >= 0 && xPos < image.width && yPos >= 0 && yPos < image.height) {
      return image.data[xPos][yPos];
    }
    
    return 0;
  }
  
  private int getBiLinInterpolatedValue(final Image image, double x, double y) {
    // How to get the 4 coords for e.g (3.7, 12.2)
    // P0: (3,12) P1: (4,12), P2: (3, 13), P3: (4,13)
    
    return 0;
  }
  
}
