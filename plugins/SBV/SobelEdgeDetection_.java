import at.sschmid.hcc.sbv1.image.AbstractPlugIn;
import at.sschmid.hcc.sbv1.image.Image;

public final class SobelEdgeDetection_ extends AbstractPlugIn {
  
  @Override
  public void process(final Image image) {
    addResult(image.edges(), pluginName);
  }
  
}
