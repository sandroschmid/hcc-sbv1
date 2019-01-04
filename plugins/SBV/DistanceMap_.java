import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.ImageTransferFunctions;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.registration.DistanceMetric;
import at.sschmid.hcc.sbv1.image.segmentation.BinaryThreshold;
import ij.gui.GenericDialog;

import java.util.Arrays;

public final class DistanceMap_ extends AbstractUserInputPlugIn<DistanceMap_.Input> {
  
  private static final int T_MIN = 100;
  private static final int T_MAX = 255;
  private static final int BG = 0;
  private static final int FG = 255;
  
  @Override
  public void process(final Image image) {
    final Image binary = image.binary(input.binaryThreshold);
    final Image edges = binary.edges().binary(new BinaryThreshold(1, 0, image.maxColor));
//    final Image distanceMapResult = binary.distanceMap(input.distanceMetric).asImage();
    final Image distanceMapEdgesResult = edges.distanceMap(input.distanceMetric).asImage();

//    addResult(binary, String.format("%s - binary", pluginName));
//    addResult(distanceMapResult, String.format("%s - distance map (%s)", pluginName, input.distanceMetric));
    addResult(edges, String.format("%s - edges", pluginName));
    final String name = String.format("%s - distance map (edges, %s)", pluginName, input.distanceMetric);
    addResult(distanceMapEdgesResult, name);
  
    final int[] histogramEqualization = ImageTransferFunctions.GetHistogramEqualizationTF2(distanceMapEdgesResult);
    addResult(distanceMapEdgesResult.transformation().transfer(histogramEqualization).getResult(), name);
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addSlider("Threshold Min", 0, 255, T_MIN);
    dialog.addSlider("Threshold Max", 0, 255, T_MAX);
    dialog.addSlider("Background", 0, 255, BG);
    dialog.addSlider("Foreground", 0, 255, FG);
    dialog.addChoice("Distance Metric",
        Arrays.stream(DistanceMetric.values()).map(Enum::toString).toArray(String[]::new),
        DistanceMetric.Manhattan.toString());
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    final int tMin = (int) dialog.getNextNumber();
    final int tMax = (int) dialog.getNextNumber();
    final BinaryThreshold binaryThreshold = new BinaryThreshold(tMin,
        tMax <= tMin ? null : tMax,
        (int) dialog.getNextNumber(),
        (int) dialog.getNextNumber());
  
    return new Input(binaryThreshold, DistanceMetric.values()[dialog.getNextChoiceIndex()]);
  }
  
  final class Input {
    
    private final BinaryThreshold binaryThreshold;
    private final DistanceMetric distanceMetric;
  
    Input(final BinaryThreshold binaryThreshold, final DistanceMetric distanceMetric) {
      this.binaryThreshold = binaryThreshold;
      this.distanceMetric = distanceMetric;
    }
    
    @Override
    public String toString() {
      return String.format("DistanceMap {\n  binaryThreshold=%s,\n  distanceMetric=%s\n}",
          binaryThreshold,
          distanceMetric);
    }
    
  }
  
}
