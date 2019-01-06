import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.imagej.ImageJUtility;
import at.sschmid.hcc.sbv1.image.segmentation.Segment;
import at.sschmid.hcc.sbv1.utility.Point;
import ij.gui.GenericDialog;

import java.util.Collection;
import java.util.List;

public final class Segment_ extends AbstractUserInputPlugIn<Segment_.Input> {

//  @Override
//  protected int getSetupMask() {
//    return super.getSetupMask() + ROI_REQUIRED;
//  }
  
  @Override
  protected void process(final Image image) {
    final Collection<Point> seeds = ImageJUtility.getSeedPoints(imagePlus);
    final Image maskWithAllSegments = seeds.isEmpty() ? automatic(image) : userSelection(image, seeds);
  
    addResult(maskWithAllSegments, String.format("%s - Mask with all segments", pluginName));
    addResult(image.calculation(maskWithAllSegments).and(), String.format("%s - All segments", pluginName));
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    final String[] sizeChoices = new String[] { "9", "21", "51", "101", "201" };
    dialog.addChoice("Segment width", sizeChoices, sizeChoices[sizeChoices.length / 2]);
    dialog.addChoice("Segment height", sizeChoices, sizeChoices[sizeChoices.length / 2]);
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    return new Input(Integer.valueOf(dialog.getNextChoice()), Integer.valueOf(dialog.getNextChoice()));
  }
  
  private Image automatic(final Image image) {
    final List<Segment> segments = image.getSegments(input.segmentWidth, input.segmentHeight);
    Image maskWithAllSegments = new Image(image.width, image.height);
    for (final Segment segment : segments) {
      maskWithAllSegments = maskWithAllSegments.calculation(segment.mask()).or();
    }
    
    return maskWithAllSegments;
  }
  
  private Image userSelection(final Image image, final Collection<Point> seeds) {
    final Segment.Builder segmentBuilder = image.segment().width(input.segmentWidth).height(input.segmentHeight);
    Image maskWithAllSegments = new Image(image.width, image.height);
    for (final Point seed : seeds) {
      final Segment segment = segmentBuilder.origin(seed).build();
      addResult(segment, String.format("Segment with origin=%s", seed));
      maskWithAllSegments = maskWithAllSegments.calculation(segment.mask()).or();
    }
    
    return maskWithAllSegments;
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
