import at.sschmid.hcc.sbv1.image.Histogram;
import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.segmentation.OptimalThreshold;
import ij.IJ;
import ij.gui.GenericDialog;

public final class OptimalThreshold_ extends AbstractUserInputPlugIn<OptimalThreshold_.Input> {
  
  @Override
  protected void process(final Image image) {
    final OptimalThreshold optimalThreshold = image.histogram().optimalThreshold(input.version);
    final boolean isAll = "All".equals(input.whichOne);
    if (isAll || "Global".equals(input.whichOne)) {
      global(image, optimalThreshold);
    }
  
    if (isAll || "Local".equals(input.whichOne)) {
      local(image, optimalThreshold);
    }
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addChoice("Version", new String[] { "v1", "v2" }, "v2");
    dialog.addChoice("Which?", new String[] { "All", "Global", "Local" }, "All");
    final String[] sizeChoices = new String[] { "9", "21", "51", "101", "201" };
    final String defaultSizeChoice = "51";
    dialog.addChoice("Segment width", sizeChoices, defaultSizeChoice);
    dialog.addChoice("Segment height", sizeChoices, defaultSizeChoice);
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    return new Input(dialog.getNextChoiceIndex() + 1,
        dialog.getNextChoice(),
        Integer.valueOf(dialog.getNextChoice()),
        Integer.valueOf(dialog.getNextChoice()));
  }
  
  private void global(final Image image, final OptimalThreshold optimalThreshold) {
    final int globalThreshold = optimalThreshold.globalValue();
    final Image globalMask = optimalThreshold.globalMask();
    final Image globalObjects = image.calculation(globalMask).and();
    final Image globalDiff = image.calculation(globalObjects).difference();
    
    addResult(globalMask, String.format("%s - global mask", pluginName));
    addResult(globalObjects, String.format("%s - globally detected objects", pluginName));
    addResult(globalDiff, String.format("%s - globally detected objects (diff)", pluginName));
    
    IJ.log(String.format("Global optimal threshold: %d", globalThreshold));
    final Histogram diffHistogram = globalDiff.histogram();
    IJ.log(String.format("Global diff - minColor=%d, maxColor=%d",
        diffHistogram.getMinColor(),
        diffHistogram.getMaxColor()));
  }
  
  private void local(final Image image, final OptimalThreshold optimalThreshold) {
    final Image localMask = optimalThreshold.localMask(input.segmentWidth, input.segmentHeight);
    final Image localObjects = image.calculation(localMask).and();
    final Image localDiff = image.calculation(localObjects).difference();
    
    addResult(localMask, String.format("%s - local mask", pluginName));
    addResult(localObjects, String.format("%s - locally detected objects", pluginName));
    addResult(localDiff, String.format("%s - locally detected objects (diff)", pluginName));
    
    final Histogram diffHistogram = localDiff.histogram();
    IJ.log(String.format("Local diff - minColor=%d, maxColor=%d",
        diffHistogram.getMinColor(),
        diffHistogram.getMaxColor()));
  }
  
  static final class Input {
  
    private final int version;
    private final String whichOne;
    private final int segmentWidth;
    private final int segmentHeight;
  
    private Input(final int version, final String whichOne, final int segmentWidth, final int segmentHeight) {
      this.version = version;
      this.whichOne = whichOne;
      this.segmentWidth = segmentWidth;
      this.segmentHeight = segmentHeight;
    }
    
    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder("Optimal Threshold (v")
          .append(version)
          .append(") {\n  whichOne=")
          .append(whichOne);
  
      if ("All".equals(whichOne) || "Local".equals(whichOne)) {
        builder.append(",\n  segmentWidth=").append(segmentWidth).append(",\n  segmentHeight=").append(segmentHeight);
      }
  
      return builder.append("\n}").toString();
    }
    
  }
  
}
