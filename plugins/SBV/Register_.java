import at.sschmid.hcc.sbv1.image.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.ImageTransformation;
import at.sschmid.hcc.sbv1.image.Transformations;
import ij.gui.GenericDialog;

public final class Register_ extends AbstractUserInputPlugIn<Register_.Input> {
  
  private static final ImageTransformation.TranslationMode TRANSLATION_MODE =
      ImageTransformation.TranslationMode.NearestNeighbour;
  private static final double DEFAULT_TRANS_X = 3.1416;
  private static final double DEFAULT_TRANS_Y = -9.9999;
  private static final double DEFAULT_ROTATION = 31.7465;
  
  @Override
  public void process(final Image image) {
    final ImageTransformation imageTransformation = new ImageTransformation(image);
    final Image transformedImage = imageTransformation.transform(input.getTransformations(), TRANSLATION_MODE)
        .getResult();
  
    addResult(transformedImage, String.format("%s - transformed image (t,r)", pluginName));
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Translate X", DEFAULT_TRANS_X, 4);
    dialog.addNumericField("Translate Y", DEFAULT_TRANS_Y, 4);
    dialog.addNumericField("Rotation (deg)", DEFAULT_ROTATION, 4);
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    return new Input(dialog.getNextNumber(),
        dialog.getNextNumber(),
        dialog.getNextNumber());
  }
  
  public static class Input {
    
    private final double transX;
    private final double transY;
    private final double rotDeg;
    
    private Input(final double transX,
                  final double transY,
                  final double rotDeg) {
      this.transX = transX;
      this.transY = transY;
      this.rotDeg = rotDeg;
    }
    
    public Transformations getTransformations() {
      return new Transformations().translate(transX, transY).rotate(rotDeg);
    }
    
  }
  
}
