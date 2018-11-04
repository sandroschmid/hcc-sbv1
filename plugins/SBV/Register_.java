import at.sschmid.hcc.sbv1.image.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.ImageTransform;
import at.sschmid.hcc.sbv1.image.Transformation;
import ij.gui.GenericDialog;

public final class Register_ extends AbstractUserInputPlugIn<Transformation> {
  
  private static final Transformation defaults = new Transformation(3.1416, -9.9999, 31.7465);
  
  @Override
  public void process(final Image image) {
    final ImageTransform imageTransform = new ImageTransform(image);
    final Image transformedImage = imageTransform.transform(input).getResult();
    
    addResult(transformedImage, String.format("%s - transformed image", pluginName));
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Translate X", defaults.translationX, 4);
    dialog.addNumericField("Translate Y", defaults.translationY, 4);
    dialog.addNumericField("Rotation", defaults.rotation, 4);
  }
  
  @Override
  protected Transformation getInput(final GenericDialog dialog) {
    return new Transformation(dialog.getNextNumber(), dialog.getNextNumber(), dialog.getNextNumber());
  }
  
}
