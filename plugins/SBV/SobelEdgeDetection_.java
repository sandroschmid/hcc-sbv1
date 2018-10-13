import at.sschmid.hcc.sbv1.ConvolutionFilter;
import at.sschmid.hcc.sbv1.ImageJUtility;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class SobelEdgeDetection_ implements PlugInFilter {

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
		double[][] inDataArrDbl = ImageJUtility.convertToDoubleArr2D(inDataArrInt, width, height);
		
		double[][] resultEdgeImage = ConvolutionFilter.ApplySobelEdgeDetection(inDataArrDbl, width, height);

		ImageJUtility.showNewImage(resultEdgeImage, width, height, "result after hor/vert sobel");
	} // run

	void showAbout() {
		IJ.showMessage("About Template_...", "this is a PluginFilter template\n");
	} // showAbout

} // class SobelEdgeDetection_
