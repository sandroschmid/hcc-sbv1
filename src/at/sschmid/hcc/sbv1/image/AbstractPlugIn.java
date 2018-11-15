package at.sschmid.hcc.sbv1.image;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractPlugIn implements PlugInFilter {
  
  private static final String ARG_SHOW_ABOUT = "about";
  private static final String INPUT_IMAGE_NAME_FORMAT = "Original image for '%s'";
  private static final String RESULT_IMAGE_NAME_FORMAT = "Result %02d for '%s'";
  
  protected final String pluginName;
  
  private final List<Image> results = new ArrayList<>();
  
  public AbstractPlugIn() {
    final String className = getClass().getSimpleName();
    this.pluginName = className.replaceAll(Pattern.quote("_"), "");
  }
  
  public final int setup(final String arg, final ImagePlus imp) {
    if (ARG_SHOW_ABOUT.equals(arg)) {
      showAbout();
      return DONE;
    }
    
    return DOES_8G + DOES_STACKS + SUPPORTS_MASKING;
  }
  
  @Override
  public void run(final ImageProcessor imageProcessor) {
    final Image image = getImage(imageProcessor);
    process(image);
    showResults();
  }
  
  protected final void addResult(final Image image) {
    if (!image.hasName()) {
      final int resultNumber = results.size() + 1;
      image.withName(String.format(RESULT_IMAGE_NAME_FORMAT, resultNumber, pluginName));
    }
  
    results.add(image);
  }
  
  protected final void addResult(final Image image, final String name) {
    results.add(image.withName(name));
  }
  
  protected final void addResult(final ImageGenerator transformation) {
    addResult(transformation.getResult());
  }
  
  protected final void addResult(final ImageGenerator transformation, final String name) {
    addResult(transformation.getResult(), name);
  }
  
  protected abstract void process(final Image image);
  
  private void showAbout() {
    IJ.showMessage("SBV", "This is a custom plugin filter for SBV.\n");
  }
  
  private Image getImage(final ImageProcessor imageProcessor) {
    final String imageName = String.format(INPUT_IMAGE_NAME_FORMAT, pluginName);
    
    final byte[] pixels = (byte[]) imageProcessor.getPixels();
    final int width = imageProcessor.getWidth();
    final int height = imageProcessor.getHeight();
    
    final int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);
    
    return new Image(imageName, inDataArrInt, width, height);
  }
  
  private void showResults() {
    results.forEach(Image::show);
  }
  
}
