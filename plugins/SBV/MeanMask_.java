import at.sschmid.hcc.sbv1.image.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.ConvolutionFilter;
import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.ImageJUtility;
import ij.gui.GenericDialog;

public final class MeanMask_ extends AbstractUserInputPlugIn<Integer> {
  
  private static final int defaultRadius = 4;
  
  @Override
  protected void process(final Image image) {
    final double[][] inDataArrDbl = ImageJUtility.convertToDoubleArr2D(image);
    final double[][] kernel = ConvolutionFilter.GetMeanMask(input);
    final double[][] resultImg = ConvolutionFilter.ConvolveDoubleNorm(inDataArrDbl,
        image.width,
        image.height,
        kernel,
        input);
    
    ImageJUtility.showNewImage(resultImg,
        image.width,
        image.height,
        String.format("%s with r=%d", pluginName, input));
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Radius", defaultRadius, 0);
  }
  
  @Override
  protected Integer getInput(final GenericDialog dialog) {
    return (int) dialog.getNextNumber();
  }
  
}
