import at.sschmid.hcc.sbv1.image.AbstractPlugIn;
import at.sschmid.hcc.sbv1.image.ConvolutionFilter;
import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.ImageJUtility;

public final class SobelEdgeDetection_ extends AbstractPlugIn {
  
  @Override
  public void process(final Image image) {
    final double[][] inDataArrDbl = ImageJUtility.convertToDoubleArr2D(image);
    final double[][] resultEdgeImage = ConvolutionFilter.ApplySobelEdgeDetection(inDataArrDbl, image.width, image.height);

    ImageJUtility.showNewImage(resultEdgeImage, image.width, image.height, pluginName);
  }
  
}
