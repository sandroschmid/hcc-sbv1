import at.sschmid.hcc.sbv1.image.ConvolutionFilter;
import at.sschmid.hcc.sbv1.image.ImageJUtility;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class MeanMask_ implements PlugInFilter {
  
  public int setup(String arg, ImagePlus imp) {
    if (arg.equals("about")) {
      showAbout();
      return DONE;
    }
    return DOES_8G + DOES_STACKS + SUPPORTS_MASKING;
  } // setup
  
  public void run(ImageProcessor ip) {
    byte[] pixels = (byte[]) ip.getPixels();
    int width = ip.getWidth();
    int height = ip.getHeight();
    int tgtRadius = 4; // mask has 9 pxls
    
    // user to define radius
    GenericDialog gd = new GenericDialog("Select a radius");
    gd.addNumericField("Radius", tgtRadius, 0);
    gd.showDialog();
    
    if (gd.wasCanceled()) {
      return;
    }
    
    tgtRadius = (int) gd.getNextNumber();
    
    int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);
    double[][] inDataArrDbl = ImageJUtility.convertToDoubleArr2D(inDataArrInt, width, height);
    
    double[][] kernel = ConvolutionFilter.GetMeanMask(tgtRadius);
    double[][] resultImg = ConvolutionFilter.ConvolveDoubleNorm(inDataArrDbl, width, height, kernel, tgtRadius);
    
    ImageJUtility.showNewImage(resultImg, width, height, "mean with kernel r=" + tgtRadius);
  } // run
  
  void showAbout() {
    IJ.showMessage("About Template_...", "Mean Mask\n");
  } // showAbout
  
} //class MeanMask_
