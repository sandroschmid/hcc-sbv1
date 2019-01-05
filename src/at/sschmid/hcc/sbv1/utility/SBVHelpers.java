package at.sschmid.hcc.sbv1.utility;

public class SBVHelpers {
  
  public static int getBilinearInterpolatedValue(int[][] inArr, double newX, double newY, int width, int height) {
    int X0 = (int) (newX);
    int Y0 = (int) (newY);
    
    //get delta
    double deltaX = newX - X0;
    double deltaY = newY - Y0;
    
    //run through the four nearest coordinates
    
    double result = (1 - deltaX) * (1 - deltaY) * inArr[X0][Y0];
    
    if (width > X0 + 1) {
      result += deltaX * (1 - deltaY) * inArr[X0 + 1][Y0];
    }
  
    if (height > Y0 + 1) {
      result += (1 - deltaX) * deltaY * inArr[X0][Y0 + 1];
    }
  
    if ((width > X0 + 1) && (height > Y0 + 1)) {
      result += deltaX * deltaY * inArr[X0 + 1][Y0 + 1];
    }
    
    return (int) (result + 0.5);
  }
  
  public static int[][] transformImage(int[][] inImg,
                                       int width,
                                       int height,
                                       double transX,
                                       double transY,
                                       double rotAngle) {
    
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
        // TODO bilinear interpolation
        int nnX = (int) (posX + 0.5);
        int nnY = (int) (posY + 0.5);
        
        //6) assign value from original image inImg if inside the image boundaries
        if (nnX >= 0 && nnX < width && nnY >= 0 && nnY < height) {
          resultImg[x][y] = inImg[nnX][nnY];
        } else {
          resultImg[x][y] = 255;
        }
      }
    }
  
    return resultImg;
  }
  
}
