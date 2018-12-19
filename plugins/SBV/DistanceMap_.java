import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.registration.DistanceMap;
import at.sschmid.hcc.sbv1.image.segmentation.BinaryThreshold;
import ij.gui.GenericDialog;

import java.util.Arrays;

public final class DistanceMap_ extends AbstractUserInputPlugIn<DistanceMap_.Input> {
  
  private static final int T_MIN = 124;
  private static final int T_MAX = 230;
  private static final int BG = 0;
  private static final int FG = 255;
  
  @Override
  public void process(final Image image) {
    final Image binary = image.binary(input.binaryThreshold);
    final Image edges = binary.edges();
    final DistanceMap distanceMap = edges.distanceMap(input.distanceMetric);
    final Image distanceMapResult = distanceMap.asImage();
    
    addResult(binary, String.format("%s - binary", pluginName));
    addResult(edges, String.format("%s - edges", pluginName));
    addResult(distanceMapResult, String.format("%s - distance map (%s)", pluginName, input.distanceMetric));
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Threshold Min", T_MIN, 0);
    dialog.addNumericField("Threshold Max", T_MAX, 0);
    dialog.addNumericField("Background", BG, 0);
    dialog.addNumericField("Foreground", FG, 0);
    dialog.addChoice("Distance Metric",
        Arrays.stream(DistanceMap.DistanceMetric.values()).map(Enum::toString).toArray(String[]::new),
        DistanceMap.DistanceMetric.Manhattan.toString());
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    final int tMin = (int) dialog.getNextNumber();
    final int tMax = (int) dialog.getNextNumber();
    final BinaryThreshold binaryThreshold = new BinaryThreshold(tMin,
        tMax <= tMin ? null : tMax,
        (int) dialog.getNextNumber(),
        (int) dialog.getNextNumber());
    
    return new Input(binaryThreshold, DistanceMap.DistanceMetric.values()[dialog.getNextChoiceIndex()]);
  }
  
  final class Input {
    
    private final BinaryThreshold binaryThreshold;
    private final DistanceMap.DistanceMetric distanceMetric;
    
    Input(final BinaryThreshold binaryThreshold,
          final DistanceMap.DistanceMetric distanceMetric) {
      this.binaryThreshold = binaryThreshold;
      this.distanceMetric = distanceMetric;
    }
    
    @Override
    public String toString() {
      return "Input {" +
          "\n  binaryThreshold=" + binaryThreshold +
          ",\n  distanceMetric=" + distanceMetric +
          "\n}";
    }
    
  }
  
}
