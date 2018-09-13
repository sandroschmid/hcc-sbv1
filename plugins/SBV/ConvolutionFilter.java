
public class ConvolutionFilter {

	public static double[][] ConvolveDoubleNorm(double[][] inputImg, int width, int height, double[][] kernel, int radius, int numOfIterations) {
	  double[][] returnImg = inputImg;
	  for(int iterCount = 0; iterCount < numOfIterations; iterCount++) {
		  returnImg = ConvolutionFilter.ConvolveDoubleNorm(returnImg, width, height, kernel, radius);
	  }
	  
	  return returnImg;
	}
	
	public static double[][] ConvolveDoubleNorm(double[][] inputImg, int width, int height, double[][] kernel, int radius) {
		double[][] returnImg = new double[width][height];
		
		return returnImg;
	}
	
	public static double[][] ConvolveDouble(double[][] inputImg, int width, int height, double[][] kernel, int radius) {
		double[][] returnImg = new double[width][height];
				
		return returnImg;
	}
	
	public static double[][] GetMeanMask(int tgtRadius) {
		int size = 2 * tgtRadius + 1;
		double[][] kernelImg = new double[size][size];
				
		return kernelImg;
	}
	
    public static double[][] GetGaussMask(int tgtRadius, double sigma) {
    	int size = 2 * tgtRadius + 1;
		double[][] kernelImg = new double[size][size];
						
		return kernelImg;
	}
    
    public static double[][] ApplySobelEdgeDetection(double[][] inputImg, int width, int height) {
    	double[][] returnImg = new double[width][height];
    	double[][] sobelV = new double[][]{{1.0, 0.0, -1.0}, {2.0, 0.0, -2.0}, {1.0, 0.0, -1.0}};
		double[][] sobelH = new double[][]{{1.0, 2.0, 1.0}, {0.0, 0.0, 0.0}, {-1.0, -2.0, -1.0}};
    	
		int radius = 1;
		double maxGradient = 0.0;
						
		return returnImg;
    }
	
	
}
