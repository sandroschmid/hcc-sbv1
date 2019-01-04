import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.resampling.Interpolation;
import at.sschmid.hcc.sbv1.image.resampling.Transformation;
import at.sschmid.hcc.sbv1.image.resampling.Transformations;
import ij.gui.GenericDialog;

import java.util.regex.Pattern;

public final class Transform_ extends AbstractUserInputPlugIn<Transform_.Input> {
  
  private static final Interpolation.Mode INTERPOLATION_MODE = Interpolation.Mode.BiLinear;
  private static final double DEFAULT_TRANS_X = 23.1416;
  private static final double DEFAULT_TRANS_Y = -49.9999;
  private static final double DEFAULT_ROTATION = 31.7465;
  private static final double DEFAULT_SCALE = 1.5;
  private static final String DEFAULT_ORDER = "t,r,s";
  
  @Override
  public void process(final Image image) {
    final Transformation transformation = new Transformation(image);
    final Image transformedImage = transformation.transform(input.getTransformations(), INTERPOLATION_MODE)
        .getResult();
    
    addResult(transformedImage, String.format("%s - transformed image (%s)", pluginName, input.order));
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Translate X", DEFAULT_TRANS_X, 4);
    dialog.addNumericField("Translate Y", DEFAULT_TRANS_Y, 4);
    dialog.addNumericField("Rotation (deg)", DEFAULT_ROTATION, 4);
    dialog.addNumericField("Scale", DEFAULT_SCALE, 2);
    dialog.addStringField("Order", DEFAULT_ORDER);
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    return new Input(dialog.getNextNumber(),
        dialog.getNextNumber(),
        dialog.getNextNumber(),
        dialog.getNextNumber(),
        dialog.getNextString());
  }
  
  public static class Input {
    
    private final double transX;
    private final double transY;
    private final double rotDeg;
    private final double scaleFactor;
    private final String order;
    
    private Input(final double transX,
                  final double transY,
                  final double rotDeg,
                  final double scaleFactor,
                  final String order) {
      this.transX = transX;
      this.transY = transY;
      this.rotDeg = rotDeg;
      this.scaleFactor = scaleFactor;
      this.order = order;
    }
    
    public Transformations getTransformations() {
      final String[] orderParts = this.order.split(Pattern.quote(","));
      if (orderParts.length != 3) {
        throw new IllegalArgumentException("Invalid order");
      }
      
      final Transformations transformations = new Transformations();
      for (final String part : orderParts) {
        switch (part.trim()) {
          case "t":
            transformations.translate(transX, transY);
            break;
          case "r":
            transformations.rotate(rotDeg);
            break;
          case "s":
            if (scaleFactor > 0) {
              transformations.scale(scaleFactor);
            }
            break;
          default:
            throw new IllegalArgumentException("Invalid order");
        }
      }
      
      return transformations;
    }
  
    @Override
    public String toString() {
      return String.format("Transform {transX=%s, transY=%s, rotDeg=%s, scaleFactor=%s, order='%s'}",
          transX,
          transY,
          rotDeg,
          scaleFactor,
          order);
    }
  
  }
  
}
