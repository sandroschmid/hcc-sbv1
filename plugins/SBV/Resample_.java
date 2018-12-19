import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.resampling.Interpolation;
import at.sschmid.hcc.sbv1.image.resampling.Transformations;
import ij.gui.GenericDialog;

public final class Resample_ extends AbstractUserInputPlugIn<Double> {
  
  private static final double defaultScale = 4d;
  
  @Override
  public void process(final Image image) {
    final Image scaledImgNN = image.transformation()
        .transform(new Transformations().scale(input), Interpolation.Mode.NearestNeighbour)
        .getResult();
    
    final Image scaledImgBiLin = image.transformation()
        .transform(new Transformations().scale(input), Interpolation.Mode.BiLinear)
        .getResult();
    
    addResult(scaledImgNN, String.format("%s - NN", pluginName));
    addResult(scaledImgBiLin, String.format("%s - Bi-Linear", pluginName));
    addResult(scaledImgNN.calculation(scaledImgBiLin).difference(), String.format("%s - Diff", pluginName));
    addResult(scaledImgNN.checkerboard(scaledImgBiLin));
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Scale", defaultScale, 2);
  }
  
  @Override
  protected Double getInput(final GenericDialog dialog) {
    return dialog.getNextNumber();
  }
  
}
