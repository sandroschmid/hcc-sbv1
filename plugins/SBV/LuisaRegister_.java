import at.sschmid.hcc.sbv1.image.imagej.ImageJUtility;
import at.sschmid.hcc.sbv1.utility.SBVHelpers;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class LuisaRegister_ implements PlugInFilter {
  
  public int[][] transformImage(int[][] inImg, int width, int height, double transX, double transY, double rotAngle) {
    int[][] resultImg = new int[width][height];
    
    // prepare cross theta, sin theta
    double cosTheta = Math.cos(Math.toRadians(-rotAngle)); // - wegen Backward mapping
    double sinTheta = Math.sin(Math.toRadians(-rotAngle));
    double widthHalf = width / 2.0;
    double heightHalf = height / 2.0;
    // iterate over all pixels and calc value utuilizing backward mapping
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        
        // rotate
        // double posX = x - widthHalf;
        // double posY = y - heightHalf;
        
        double posX = (x - widthHalf) * cosTheta + (y - heightHalf) * sinTheta;
        double posY = -(x - widthHalf) * sinTheta + (y - heightHalf) * cosTheta;
        
        // translate
        posX -= transX; // wegen backward mapping
        posY -= transY;
        
        // move back to te top left corner
        posX = posX + widthHalf;
        posY = posY + heightHalf;
        
        // during the process use NN Interpolation
        int nnX = (int) (posX + 0.5);
        int nnY = (int) (posY + 0.5);
        
        // assign value from original image if inside image boundaries
        if (nnX >= 0 && nnY >= 0 && width > nnX && height > nnY) {
          resultImg[x][y] = inImg[nnX][nnY];
        }
        
      }
    }
    
    return resultImg;
  }
  
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
    
    int[][] transformedImg = transformImage(inDataArrInt, width, height, transX, transY, rotAngle);
    
    // TOT get SSE metric
    double initError = getSSEerrorMetric(inDataArrInt, transformedImg, width, height);
    
    ImageJUtility.showNewImage(transformedImg, width, height, "transformed");
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
    int[][] correctedImg = new int[0][0];
    
    for (int n = 0; n < numOfOptimizationRuns; n++) {
      // search from -20 to +20, namely -20, -18,...
      for (int xIdx = -searchRad; xIdx < searchRad; xIdx++) {
        for (int yIdx = -searchRad; yIdx < searchRad; yIdx++) {
          for (int rotIdx = -searchRad; rotIdx < searchRad; rotIdx++) {
            double currTx = actMidTx + xIdx * stepWidthTranslation;
            double currTy = actMidTy + yIdx * stepWidthTranslation;
            double currRot = actMidRot + rotIdx * stepWidthRotation;
            
            correctedImg = SBVHelpers.transformImage(transformedImg, width, height, currTx, currTy, currRot);
            double currError = getSSEerrorMetric(inDataArrInt, correctedImg, width, height);
            
            if (currError > bestSSE) {
              bestSSE = currError;
              bestTx = currTx;
              bestTy = currTy;
              bestRot = currRot;
              IJ.log("new best SSE=" + bestSSE);
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
    // TODO use BLI for final image
    //int[][] bestResImg = transformImage(transformedImg, width, height, bestTx, bestTy, bestRot);
    //int[][] bestResImg = SBVHelpers.transformImage(transformedImg, width, height, bestTx, bestTy, bestRot);
    
    int[][] bestResImg = new int[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        bestResImg[x][y] = SBVHelpers.getBilinearInterpolatedValue(correctedImg, x, y, width, height);
      }
    }
  
    ImageJUtility.showNewImage(bestResImg, width, height, "registered SSE = " + bestSSE);
  } // run
  
  void showAbout() {
    IJ.showMessage("About Template_...", "this is a PluginFilter template\n");
  } // showAbout
  
}