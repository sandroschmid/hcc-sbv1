import at.sschmid.hcc.sbv1.image.AbstractPlugIn;
import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.ImageTransferFunctions;

public final class BinaryIntervalThreshold_ extends AbstractPlugIn {
  
  private static final int T_MIN = 124;
  private static final int T_MAX = 230;
  
  @Override
  public void process(final Image image) {
    final int[] threshTF = ImageTransferFunctions.GetBinaryThresholdTF(255, T_MIN, T_MAX, 255, 0);
    final Image resultImg = image.transformation().transfer(threshTF).getResult();
    
    addResult(resultImg, String.format("Binary threshold, interval between [%d,%d]", T_MIN, T_MAX));
  }
  
}
