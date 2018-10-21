import at.sschmid.hcc.sbv1.ImageJUtility;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Resample_ implements PlugInFilter {

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

		int newWidth = width;
		int newHeight = height;
		
		// first request target scale factor from user
		GenericDialog gd = new GenericDialog("Resample - Scale");
		gd.addNumericField("Scale", 1.0, 2);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		
		double tgtScaleFactor = gd.getNextNumber();
		if (tgtScaleFactor < 0.01d || tgtScaleFactor > 10d) {
			return;
		}
		
		int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);
		
		newWidth = (int)(width * tgtScaleFactor + 0.5); // arithm round
		newHeight = (int)(height * tgtScaleFactor + 0.5);

		// forward mapping
//		double scaleFactorX = newWidth / (double) width;
//		double scaleFactorY = newHeight / (double) height;
		
		double scaleFactorX = (newWidth - 1.0) / (double) width;
		double scaleFactorY = (newHeight - 1.0) / (double) height;
		
		System.out.println("tgtScale=" + tgtScaleFactor + " sX=" + scaleFactorX + " sY=" + scaleFactorY);
		System.out.println("new width=" + newWidth + " new height=" + newHeight);
		
		// fill new img
		int[][] scaledImg = new int[newWidth][newHeight];
		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				// calc new coordinates
				double newX = x / scaleFactorX; // coord in input image A utilizing backward mapping. forward mapping works only when shrinking the size
				double newY = y / scaleFactorY;

				int resultVal = getNNInterpolatedValue(inDataArrInt, width, height, newX, newY); // forward mapping: use x/y coords instead of newX/newY
//				int resultVal = getBiLinInterpolatedValue(inDataArrInt, width, height, newX, newY);
				
				// forward mapping
//				int roundedNewX = (int)(newX + 0.5);
//				int roundedNewY = (int)(newY + 0.5);
//				if (roundedNewX >= 0 && roundedNewX < width && roundedNewY >= 0 && roundedNewY < height) {
//					scaledImg[roundedNewX][roundedNewY] = resultVal;
//				}
				
				scaledImg[x][y] = resultVal;
			}
		}
		
		ImageJUtility.showNewImage(scaledImg, newWidth, newHeight, "scaled image");

	} // run
	
	private int getNNInterpolatedValue(int[][] img, int width, int height, double x, double y) {
		int xPos = (int)(x + 0.5);
		int yPos = (int)(y + 0.5);
		
		if (xPos >= 0 && xPos < width && yPos >= 0 && yPos < height) {
			return img[xPos][yPos];
		}
		
		return 0;
	}
	
	private int getBiLinInterpolatedValue(int[][] img, int width, int height, double x, double y) {
		// How to get the 4 coords for e.g (3.7, 12.2)
		// P0: (3,12) P1: (4,12), P2: (3, 13), P3: (4,13)
		
		return 0;
	}

	void showAbout() {
		IJ.showMessage("About Template_...", "this is a PluginFilter template\n");
	} // showAbout

}
