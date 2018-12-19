import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.segmentation.Neighbour;
import at.sschmid.hcc.sbv1.image.segmentation.Segmentation;
import ij.gui.GenericDialog;

import java.util.Arrays;

public final class MathMorph_ extends AbstractUserInputPlugIn<MathMorph_.Input> {
  
  private static final Neighbour NB = Neighbour.N4;
  
  @Override
  protected void process(final Image image) {
    final Segmentation segmentation = image.segmentation();
    Image result;
    switch (input.action) {
      case "Erosion":
        result = segmentation.erosion(input.neighbour, input.n);
        break;
      case "Dilation":
        result = segmentation.dilation(input.neighbour, input.n);
        break;
      case "Opening":
        result = segmentation.opening(input.neighbour, input.n);
        break;
      case "Closing":
        result = segmentation.closing(input.neighbour, input.n);
        break;
      default:
        throw new IllegalArgumentException(String.format("Action %s is not valid", input.action));
    }
    
    addResult(result, String.format("%s (%s ,%d)", input.action, input.neighbour, input.n));
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addChoice("Action", new String[] { "Erosion", "Dilation", "Opening", "Closing" }, "Erosion");
    dialog.addChoice("Neighbour adjacency",
        Arrays.stream(Neighbour.values()).map(Enum::toString).toArray(String[]::new),
        NB.toString());
    dialog.addSlider("n", 1, 100, 5);
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    return new Input(dialog.getNextChoice(),
        Neighbour.values()[dialog.getNextChoiceIndex()],
        (int) dialog.getNextNumber());
  }
  
  static final class Input {
    
    private final String action;
    private final Neighbour neighbour;
    private final int n;
    
    Input(final String action, final Neighbour neighbour, final int n) {
      this.action = action;
      this.neighbour = neighbour;
      this.n = n;
    }
    
    @Override
    public String toString() {
      return String.format("Math. Morph. {\n  action=%s,\n  neighbour=%s, \n  n=%d\n}", action, neighbour, n);
    }
    
  }
  
}
