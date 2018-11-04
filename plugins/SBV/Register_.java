import at.sschmid.hcc.sbv1.image.ImageJUtility;
import at.sschmid.hcc.sbv1.image.ImageTransform;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Register_ implements PlugInFilter {
  
  public int setup(String arg, ImagePlus imp) {
    if (arg.equals("about")) {
      showAbout();
      return DONE;
    }
    return DOES_8G + DOES_STACKS + SUPPORTS_MASKING;
  } // setup
  
  public void run(ImageProcessor ip) {
    final byte[] pixels = (byte[]) ip.getPixels();
    final int width = ip.getWidth();
    final int height = ip.getHeight();
    
    final int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);
    
    final ImageTransform imageTransform = new ImageTransform(inDataArrInt, width, height);
//    final ImageTransform.Transformation transformation = new ImageTransform.Transformation(30, -59, 0);
    final ImageTransform.Transformation transformation = new ImageTransform.Transformation(3.14159, -9.9999, 31.7465);
    final ImageTransform.Image result = imageTransform.transform(transformation).getResult();
    
    ImageJUtility.showNewImage(result.data, width, height, "transformed image");
    
  } // run
  
  void showAbout() {
    IJ.showMessage("About Template_...", "this is a PluginFilter template\n");
  } // showAbout
  
}
