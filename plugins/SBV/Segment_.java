import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.Segment;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.imagej.ImageJUtility;
import at.sschmid.hcc.sbv1.utility.Point;
import ij.gui.GenericDialog;

import java.util.Collection;

public final class Segment_ extends AbstractUserInputPlugIn<Segment_.Input> {
  
  @Override
  protected int getSetupMask() {
    return super.getSetupMask() + ROI_REQUIRED;
  }
  
  @Override
  protected void process(final Image image) {
    final Collection<Point> seeds = ImageJUtility.getSeedPoints(imagePlus);
//    final Image result = new Image(image.width, image.height);
    final Segment.Builder segmentBuilder = image.segment().width(input.segmentWidth).height(input.segmentHeight);
    for (final Point seed : seeds) {
      final Segment segment = segmentBuilder.center(seed).build();
      addResult(segment);
    }
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    final String[] sizeChoices = new String[] { "51", "101", "201" };
    dialog.addChoice("Segment width", sizeChoices, sizeChoices[sizeChoices.length / 2]);
    dialog.addChoice("Segment height", sizeChoices, sizeChoices[sizeChoices.length / 2]);
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
      return String.format("Segment {\n  segmentWidth=%d,\n  segmentHeight=%d\n}", segmentWidth, segmentHeight);
    }
    
  }
  
}
