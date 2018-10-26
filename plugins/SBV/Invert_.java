import at.sschmid.hcc.sbv1.image.ImageJUtility;
import at.sschmid.hcc.sbv1.image.ImageTransformationFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Invert_ implements PlugInFilter {
  
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
    
    int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);
    
    // implement image inversion
    System.out.println("w=" + width + " h=" + height + " array[10,20]=" + inDataArrInt[10][20]);
    int[] invertTF = ImageTransformationFilter.GetInversionTF(255);
    int[][] resultImg = ImageTransformationFilter.GetTransformedImage(inDataArrInt, width, height, invertTF);
    
    ImageJUtility.showNewImage(resultImg, width, height, "inverted image");
  } // run
  
  void showAbout() {
    IJ.showMessage("About Template_...", "Invert image colors\n");
  } // showAbout
  
} //class Invert_
