import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import ij.gui.GenericDialog;

public final class IntervalThreshold_ extends AbstractUserInputPlugIn<IntervalThreshold_.ThresholdInput> {
  
  private static final int BG_VAL = 0;
  private static final int FG_VAL = 255;
  
  @Override
  protected void process(final Image image) {
    final Image result = calcThresh(image);
    addResult(result);
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Lower Thresh", 80, 0);
    dialog.addNumericField("Higher Thresh", 120, 0);
  }
  
  @Override
  protected ThresholdInput getInput(final GenericDialog dialog) {
    return new ThresholdInput((int) dialog.getNextNumber(), (int) dialog.getNextNumber());
  }
  
  private Image calcThresh(final Image image) {
    final Image result = new Image(image).withName(String.format("Interval Threshold %s", input.toString()));
    
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        final int currVal = image.data[x][y];
        result.data[x][y] = currVal >= input.lower && currVal <= input.upper ? FG_VAL : BG_VAL;
      }
    }
    
    return result;
  }
  
  static class ThresholdInput {
    
    private final int lower;
    private final int upper;
    
    ThresholdInput(final int lower, final int upper) {
      this.lower = lower;
      this.upper = upper;
    }
    
    @Override
    public String toString() {
      return String.format("Threshold [%d;%d]", lower, upper);
    }
    
  }
  
}
