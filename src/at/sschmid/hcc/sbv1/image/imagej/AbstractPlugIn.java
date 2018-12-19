package at.sschmid.hcc.sbv1.image.imagej;

import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.ImageGenerator;
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
  
  protected ImagePlus imagePlus;
  
  private final List<Image> results = new ArrayList<>();
  
  public AbstractPlugIn() {
    final String className = getClass().getSimpleName();
    this.pluginName = className.replaceAll(Pattern.quote("_"), "");
  }
  
  public final int setup(final String arg, final ImagePlus imp) {
    imagePlus = imp;
    if (ARG_SHOW_ABOUT.equals(arg)) {
      showAbout();
      return DONE;
    }
    
    return getSetupMask();
  }
  
  protected int getSetupMask() {
    return DOES_8G + DOES_STACKS + SUPPORTS_MASKING;
  }
  
  @Override
  public void run(final ImageProcessor imageProcessor) {
    final Image image = getImage(imageProcessor);
    process(imageProcessor, image);
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
  
  protected void process(final Image image) {
    throw new RuntimeException("Not implemented");
  }
  
  protected void process(final ImageProcessor imageProcessor, final Image image) {
    process(image);
  }
  
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
