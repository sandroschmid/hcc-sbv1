
public class ImageTransformationFilter {

	
	public static int[][] GetTransformedImage(int[][] inImg, int width, int height, int[] transferFunction) {
		int[][] returnImg = new int[width][height];
		
		
		return returnImg;
	}
	
	public static int[] GetInversionTF(int maxVal) {
		int[] transferFunction = new int[maxVal + 1];
				
		return transferFunction;
	}
	
	public static int[] GetHistogram(int maxVal, int[][] inImg, int width, int height) {
		int[] histogram = new int[maxVal + 1];
				
		return histogram;
	}
	
	public static int[] GetGammaCorrTF(int maxVal, double gamma) {
		int[] transferFunction = new int[maxVal + 1];
		
		return transferFunction;
	}
	
	public static int[] GetBinaryThresholdTF(int maxVal, int thresholdVal, int FG_VAL, int BG_VAL) {
		int[] transferFunction = new int[maxVal + 1];
		
				
		return transferFunction;
	}
	
	public static int[] GetHistogramEqualizationTF(int maxVal, int[][] inImg, int width, int height) {
		int[] returnTF = new int[maxVal + 1];
		
		return returnTF;
	}
	
}
