import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.imagej.AbstractPlugIn;

public final class FilterTemplate_ extends AbstractPlugIn {
  
  @Override
  protected void process(final Image image) {
    addResult(image, pluginName);
  }
  
}
