package at.sschmid.hcc.sbv1.image.imagej;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.process.ImageProcessor;

import java.util.logging.Logger;

public abstract class AbstractUserInputPlugIn<T> extends AbstractPlugIn {
  
  private static final Logger LOGGER = Logger.getLogger(AbstractUserInputPlugIn.class.getName());
  
  protected T input;
  
  @Override
  public void run(final ImageProcessor imageProcessor) {
    final GenericDialog dialog = new GenericDialog(pluginName);
    setupDialog(dialog);
    dialog.showDialog();
    if (dialog.wasCanceled()) {
      LOGGER.info(String.format("Cancelled '%s' due to missing user input.", pluginName));
      return;
    }
    
    input = getInput(dialog);
    if (input instanceof Boolean
        || input instanceof Character
        || input instanceof Byte
        || input instanceof Short
        || input instanceof Integer
        || input instanceof Long
        || input instanceof Float
        || input instanceof Double
        || input instanceof Void) {
      IJ.log(String.format("Input for %s: %s", pluginName, input));
    } else {
      IJ.log(input.toString());
    }
    
    super.run(imageProcessor);
  }
  
  protected abstract void setupDialog(final GenericDialog dialog);
  
  protected abstract T getInput(final GenericDialog dialog);
  
}
