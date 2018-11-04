import at.sschmid.hcc.sbv1.image.AbstractPlugIn;
import at.sschmid.hcc.sbv1.image.Image;

public final class FilterTemplate_ extends AbstractPlugIn {
  
  @Override
  protected void process(final Image image) {
    addResult(image, pluginName);
  }
  
}
