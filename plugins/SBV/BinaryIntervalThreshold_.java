import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.segmentation.BinaryThreshold;
import ij.gui.GenericDialog;

public final class BinaryIntervalThreshold_ extends AbstractUserInputPlugIn<BinaryThreshold> {
  
  private static final int T_MIN = 124;
  private static final int T_MAX = 230;
  private static final int BG = 0;
  private static final int FG = 255;
  
  @Override
  public void process(final Image image) {
    addResult(image.binary(input), pluginName);
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Threshold Min", T_MIN, 0);
    dialog.addNumericField("Threshold Max", T_MAX, 0);
    dialog.addNumericField("Background", BG, 0);
    dialog.addNumericField("Foreground", FG, 0);
  }
  
  @Override
  protected BinaryThreshold getInput(final GenericDialog dialog) {
    return new BinaryThreshold((int) dialog.getNextNumber(),
        (int) dialog.getNextNumber(),
        (int) dialog.getNextNumber(),
        (int) dialog.getNextNumber());
  }
  
}
