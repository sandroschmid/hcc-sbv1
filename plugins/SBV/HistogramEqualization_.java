import at.sschmid.hcc.sbv1.ImageJUtility;
import at.sschmid.hcc.sbv1.ImageTransformationFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class HistogramEqualization_ implements PlugInFilter {
  
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
    
    int[][] tfs = new int[5][];
    tfs[0] = ImageTransformationFilter.GetHistogramEqualizationTF(255, inDataArrInt, width, height);
    tfs[1] = ImageTransformationFilter.GetHistogramEqualizationTF2(255, inDataArrInt, width, height);
    tfs[2] = ImageTransformationFilter.GetHistogramEqualizationTF3(255, inDataArrInt, width, height);
    tfs[3] = ImageTransformationFilter.GetHistogramEqualizationTF4(255, inDataArrInt, width, height);
    tfs[4] = ImageTransformationFilter.GetHistogramEqualizationTF5(255, inDataArrInt, width, height);
    
    for (int i = 0; i < tfs.length; i++) {
      int[] tf = tfs[i];
      if (tf == null) {
        continue;
      }

//			for (int j = 0; j < tf.length; j++) {
//				final int tfVal = tf[j];
//				if (tfVal > 255) {
//					System.out.println("tf[" + i + "][" + j + "]=" + tfVal);
//				}
//			}
      
      int[][] resultImg = ImageTransformationFilter.GetTransformedImage(inDataArrInt, width, height, tf);
      ImageJUtility.showNewImage(resultImg, width, height, "Histogram equalization " + (i + 1));
    }
    
  } // run
  
  void showAbout() {
    IJ.showMessage("About Template_...", "Histogram equalization\n");
  } // showAbout
  
} //class HistogramEqualization_
