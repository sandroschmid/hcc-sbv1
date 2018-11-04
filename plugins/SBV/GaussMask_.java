import at.sschmid.hcc.sbv1.image.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.ConvolutionFilter;
import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.ImageJUtility;
import ij.gui.GenericDialog;

public final class GaussMask_ extends AbstractUserInputPlugIn<Integer> {
  
  private static final int defaultRadius = 4;
  
  @Override
  protected void process(final Image image) {
    /*
     * https://imagej.nih.gov/ij/developer/api/ij/plugin/filter/GaussianBlur.html
     *
     * > 'Radius' means the radius of decay to exp(-0.5) ~ 61%, i.e. the standard deviation sigma of the Gaussian
     *   (this is the same as in Photoshop, ...).
     */
    final double tgtSigma = input * Math.exp(-0.5);
    final int kernelSize = 2 * input + 1;
    
    double[][] inDataArrDbl = ImageJUtility.convertToDoubleArr2D(image);
    
    double[][] kernel = ConvolutionFilter.GetGaussMask(input, tgtSigma, true);
    double[][] kernelImg = ConvolutionFilter.maskAsImage(kernel, input);
    double[][] resultImg = ConvolutionFilter.ConvolveDoubleNorm(inDataArrDbl, image.width, image.height, kernel, input);
    
    ImageJUtility.showNewImage(resultImg, image.width, image.height,
        String.format("%s - r=%d, s=%s", pluginName, input, tgtSigma));
    ImageJUtility.showNewImage(kernelImg,
        kernelSize,
        kernelSize,
        String.format("%s - Kernel r=%d s=%s", pluginName, input, tgtSigma));
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
