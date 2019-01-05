import at.sschmid.hcc.sbv1.image.Histogram;
import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.OptimalThreshold;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.segmentation.BinaryThreshold;
import ij.IJ;
import ij.gui.GenericDialog;

public final class OptimalThreshold_ extends AbstractUserInputPlugIn<OptimalThreshold_.Input> {
  
  @Override
  protected void process(final Image image) {
    final OptimalThreshold optimalThreshold = image.optimalThreshold();
    final int optimalThresholdGlobal = optimalThreshold.calculate();
    
    final Image objectsMask = image.binary(new BinaryThreshold(optimalThresholdGlobal, 0, image.maxColor));
    final Image objects = image.calculation(objectsMask).and();
    final Image diff = image.calculation(objects).difference();
    
    addResult(objectsMask, String.format("%s - detected objects (mask)", pluginName));
    addResult(objects, String.format("%s - detected objects", pluginName));
    addResult(diff, String.format("%s - detected objects (diff)", pluginName));
    
    final Histogram diffHistogram = diff.histogram();
    IJ.log(String.format("Diff minColor=%d, maxColor=%d", diffHistogram.getMinColor(), diffHistogram.getMaxColor()));
    
    IJ.log(String.format("Optimal Threshold: %d", optimalThresholdGlobal));
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    final String[] sizeChoices = new String[] { "9", "21", "51", "101" };
    dialog.addChoice("Segment width", sizeChoices, sizeChoices[0]);
    dialog.addChoice("Segment height", sizeChoices, sizeChoices[0]);
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    return new Input(Integer.valueOf(dialog.getNextChoice()), Integer.valueOf(dialog.getNextChoice()));
  }
  
  static final class Input {
    
    private final int segmentWidth;
    private final int segmentHeight;
    
    private Input(final int segmentWidth, final int segmentHeight) {
      this.segmentWidth = segmentWidth;
      this.segmentHeight = segmentHeight;
    }
    
    @Override
    public String toString() {
      return String.format("Optimal Threshold {\n  segmentWidth=%d,\n  segmentHeight=%d\n}",
          segmentWidth,
          segmentHeight);
    }
    
  }
  
}
