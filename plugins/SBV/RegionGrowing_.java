import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.imagej.ImageJUtility;
import at.sschmid.hcc.sbv1.image.segmentation.BinaryThreshold;
import at.sschmid.hcc.sbv1.image.segmentation.Neighbour;
import at.sschmid.hcc.sbv1.image.segmentation.Segmentation;
import at.sschmid.hcc.sbv1.utility.Point;
import ij.gui.GenericDialog;

import java.util.Arrays;
import java.util.Collection;

public final class RegionGrowing_ extends AbstractUserInputPlugIn<RegionGrowing_.Input> {
  
  private static final Neighbour NB = Neighbour.N8;
  private static final int T_MIN = 100;
  private static final int T_MAX = 255;
  private static final int BG = 0;
  private static final int FG = 255;
  
  @Override
  protected int getSetupMask() {
    return super.getSetupMask() + ROI_REQUIRED;
  }
  
  @Override
  protected void process(final Image image) {
    final Collection<Point> seeds = ImageJUtility.getSeedPoints(imagePlus);
    
    final Segmentation segmentation = image.segmentation();
    final Image resultGrowing = segmentation.regionGrowing(seeds, input.nb, input.bt);
    final Image resultLabelling = segmentation.regionLabelling(seeds, input.nb, input.bt);
    
    addResult(resultGrowing, String.format("%s (%s)", pluginName, input.nb));
    addResult(resultLabelling, String.format("%s (%s with labelling)", pluginName, input.nb));
    
    addResult(image.calculation(resultGrowing).and());
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addChoice("Neighbour adjacency",
        Arrays.stream(Neighbour.values()).map(Enum::toString).toArray(String[]::new),
        NB.toString());
    dialog.addSlider("Threshold Min", 0, 255, T_MIN);
    dialog.addSlider("Threshold Max", 0, 255, T_MAX);
    dialog.addSlider("Background", 0, 255, BG);
    dialog.addSlider("Foreground", 0, 255, FG);
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    final BinaryThreshold binaryThreshold = new BinaryThreshold((int) dialog.getNextNumber(),
        (int) dialog.getNextNumber(),
        (int) dialog.getNextNumber(),
        (int) dialog.getNextNumber());
    
    return new Input(Neighbour.values()[dialog.getNextChoiceIndex()], binaryThreshold);
  }
  
  static final class Input {
    
    private final Neighbour nb;
    private final BinaryThreshold bt;
    
    Input(final Neighbour nb, final BinaryThreshold bt) {
      this.nb = nb;
      this.bt = bt;
    }
    
    @Override
    public String toString() {
      return String.format("Region Growing {\n  nb=%s,\n  bt=%s\n}", nb, bt);
    }
    
  }
  
}
