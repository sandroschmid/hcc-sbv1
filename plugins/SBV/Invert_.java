import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.ImageTransferFunctions;
import at.sschmid.hcc.sbv1.image.imagej.AbstractPlugIn;

public final class Invert_ extends AbstractPlugIn {
  
  @Override
  protected void process(final Image image) {
    final int[] invertTF = ImageTransferFunctions.GetInversionTF(255);
    final Image resultImg = image.transformation().transfer(invertTF).getResult();
  
    addResult(resultImg, String.format("%s - v1", pluginName));
    addResult(image.transformation().invert(), String.format("%s - v2", pluginName));
  }
  
}
