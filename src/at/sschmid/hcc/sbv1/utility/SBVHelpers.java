package at.sschmid.hcc.sbv1.utility;

import at.sschmid.hcc.sbv1.image.Image;

public class SBVHelpers {
  
  private static final int BG = 0;
  
  public static int getNNInterpolatedValue(int[][] inImg, double posX, double posY, int width, int height) {
//    int nnX = (int) (posX + 0.5);
//    int nnY = (int) (posY + 0.5);
//
//    int result;
//    if (nnX >= 0 && nnX < width && nnY >= 0 && nnY < height) {
//      result = inImg[nnX][nnY];
//    } else {
//      result = BG;
//    }
//
//    return result;
    
    final Image image = new Image(inImg, width, height);
    return image.interpolation().getNearestNeighbourColor(posX, posY);
  }
  
  public static int getBilinearInterpolatedValue(int[][] inImg, double posX, double posY, int width, int height) {
//    int X0 = (int) (posX);
//    int Y0 = (int) (posY);
//
//    //get delta
//    double deltaX = posX - X0;
//    double deltaY = posY - Y0;
//
//    //run through the four nearest coordinates
//
//    double result = (1 - deltaX) * (1 - deltaY) * inImg[X0][Y0];
//
//    if (width > X0 + 1) {
//      result += deltaX * (1 - deltaY) * inImg[X0 + 1][Y0];
//    }
//
//    if (height > Y0 + 1) {
//      result += (1 - deltaX) * deltaY * inImg[X0][Y0 + 1];
//    }
//
//    if ((width > X0 + 1) && (height > Y0 + 1)) {
//      result += deltaX * deltaY * inImg[X0 + 1][Y0 + 1];
//    }
//
//    return (int) (result + 0.5);
    
    final Image image = new Image(inImg, width, height);
    return image.interpolation().getBiLinearColor(posX, posY);
  }
  
  public static int[][] transformImage(int[][] inImg,
                                       int width,
                                       int height,
                                       double transX,
                                       double transY,
                                       double rotAngle,
                                       boolean useBiLinear) {
    
    //allocate result image
    int[][] resultImg = new int[width][height];
    
    // prepare cos theta, sin theta
    //double cosTheta = Math.cos(Math.toRadians(-rotAngle));
    //double sinTheta = Math.sin(Math.toRadians(-rotAngle)); // - weil backgroundmapping
    double radAngle = rotAngle * Math.PI / 180;
    double cosTheta = Math.cos(radAngle);
    double sinTheta = Math.sin(radAngle);
    
    double widthHalf = width / 2.0;
    double heightHalf = height / 2.0;
    
    //1) interate over all pixels and calc value utilizing backward-mapping
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        
        double tmpposX = x - widthHalf;
        double tmpposY = y - heightHalf;
        
        //3) rotate
        double posX = tmpposX * cosTheta + tmpposY * sinTheta;
        double posY = -tmpposX * sinTheta + tmpposY * cosTheta;
        
        //4) translate
        posX -= transX;
        posY -= transY;
        
        // move origin back from center to top corner
        posX = posX + widthHalf;
        posY = posY + heightHalf;
        
        //6) get interpolated value
        if (useBiLinear) {
          resultImg[x][y] = getBilinearInterpolatedValue(inImg, posX, posY, width, height);
        } else {
          resultImg[x][y] = getNNInterpolatedValue(inImg, posX, posY, width, height);
        }
      }
    }
  
    return resultImg;
  }
  
}
