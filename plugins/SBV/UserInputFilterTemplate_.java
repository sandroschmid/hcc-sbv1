import at.sschmid.hcc.sbv1.image.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.Image;
import ij.gui.GenericDialog;

public final class UserInputFilterTemplate_ extends AbstractUserInputPlugIn<Integer> {
  
  private static final Integer defaults = 2;
  
  @Override
  protected void process(final Image image) {
    addResult(image, pluginName);
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Radius", defaults, 0);
  }
  
  @Override
  protected Integer getInput(final GenericDialog dialog) {
    return (int) dialog.getNextNumber();
  }
  
}
