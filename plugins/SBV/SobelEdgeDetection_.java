import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.imagej.AbstractPlugIn;

public final class SobelEdgeDetection_ extends AbstractPlugIn {
  
  @Override
  public void process(final Image image) {
    addResult(image.edges(), pluginName);
  }
  
}
