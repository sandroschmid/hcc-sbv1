import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.imagej.ImageJUtility;
import at.sschmid.hcc.sbv1.utility.SBVHelpers;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class LuisaRegister_ implements PlugInFilter {
  
  public int setup(String arg, ImagePlus imp) {
    if (arg.equals("about")) {
      showAbout();
      return DONE;
    }
    return DOES_8G + DOES_STACKS + SUPPORTS_MASKING;
  } // setup
  
  public double getSSEerrorMetric(int[][] img1, int[][] img2, int width, int height) {
    double SSEsum = 0.0;
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        int val1 = img1[x][y];
        int val2 = img2[x][y];
        
        SSEsum += (val1 - val2) * (val1 - val2);
        
      }
    }
    
    return SSEsum;
  }
  
  public void run(ImageProcessor ip) {
    byte[] pixels = (byte[]) ip.getPixels();
    int width = ip.getWidth();
    int height = ip.getHeight();
    int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);
    
    // define transformation params
    double transX = 10;
    double transY = 10;
    double rotAngle = 1.00;
    
    int[][] transformedImg = SBVHelpers.transformImage(inDataArrInt, width, height, transX, transY, rotAngle, true);
    ImageJUtility.showNewImage(transformedImg, width, height, "transformed");
    
    // TOT get SSE metric
    double initError = getSSEerrorMetric(inDataArrInt, transformedImg, width, height);
    
    double bestTx = 0;
    double bestTy = 0;
    double bestRot = 0;
    double bestSSE = initError;
    
    double actMidTx = bestTx;
    double actMidTy = bestTy;
    double actMidRot = bestRot;
    
    double stepWidthTranslation = 2.0;
    double stepWidthRotation = 2.0;
    
    int searchRad = 10;
    double scalePerRun = 0.9;
    int numOfOptimizationRuns = 10;
    int[][] correctedImg = null;
    
    for (int n = 0; n < numOfOptimizationRuns; n++) {
      // search from -20 to +20, namely -20, -18,...
      for (int xIdx = -searchRad; xIdx < searchRad; xIdx++) {
        for (int yIdx = -searchRad; yIdx < searchRad; yIdx++) {
          for (int rotIdx = -searchRad; rotIdx < searchRad; rotIdx++) {
            double currTx = actMidTx + xIdx * stepWidthTranslation;
            double currTy = actMidTy + yIdx * stepWidthTranslation;
            double currRot = actMidRot + rotIdx * stepWidthRotation;
            
            correctedImg = SBVHelpers.transformImage(transformedImg, width, height, currTx, currTy, currRot, false);
            double currError = getSSEerrorMetric(inDataArrInt, correctedImg, width, height);
            
            if (currError < bestSSE) {
              bestSSE = currError;
              bestTx = currTx;
              bestTy = currTy;
              bestRot = currRot;
              // IJ.log("new best SSE=" + bestSSE);
            }
          }
        }
      }
      
      stepWidthTranslation *= scalePerRun;
      stepWidthRotation *= scalePerRun;
      
      actMidTx = bestTx;
      actMidTy = bestTy;
      actMidRot = bestRot;
    }
    
    // use BLI for final image
    if (correctedImg != null) {
      int[][] bestResImgData = SBVHelpers.transformImage(transformedImg, width, height, bestTx, bestTy, bestRot, true);
      ImageJUtility.showNewImage(bestResImgData, width, height, "registered SSE = " + bestSSE);
      new Image(inDataArrInt, width, height).calculation(new Image(bestResImgData, width, height)).difference().show();
    } else {
      IJ.log("Could not find any solution");
    }
  } // run
  
  void showAbout() {
    IJ.showMessage("About Template_...", "this is a PluginFilter template\n");
  } // showAbout
  
}