package at.sschmid.hcc.sbv1.image;

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
    super.run(imageProcessor);
  }
  
  protected abstract void setupDialog(final GenericDialog dialog);
  
  protected abstract T getInput(final GenericDialog dialog);
  
}
