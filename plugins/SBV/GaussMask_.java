import at.sschmid.hcc.sbv1.ConvolutionFilter;
import at.sschmid.hcc.sbv1.ImageJUtility;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class GaussMask_ implements PlugInFilter {

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
		int tgtRadius = 4; // mask has 9 pxls
		double tgtSigma = 2;
		
		// user to define radius
		GenericDialog gd = new GenericDialog("Select a radius");
		gd.addNumericField("Radius", tgtRadius, 0);
//		gd.addNumericField("Sigma", tgtSigma, 0);
		gd.showDialog();
		
		if (gd.wasCanceled()) {
			return;
		}

		tgtRadius = (int) gd.getNextNumber();
//		tgtSigma = gd.getNextNumber();
		
		/*
		 * https://imagej.nih.gov/ij/developer/api/ij/plugin/filter/GaussianBlur.html
		 * 
		 * > 'Radius' means the radius of decay to exp(-0.5) ~ 61%, i.e. the standard deviation sigma of the Gaussian
		 *   (this is the same as in Photoshop, ...).
		 */
		tgtSigma = tgtRadius * Math.exp(-0.5);

		int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);
		double[][] inDataArrDbl = ImageJUtility.convertToDoubleArr2D(inDataArrInt, width, height);
		
		double[][] kernel = ConvolutionFilter.GetGaussMask(tgtRadius, tgtSigma);
		double[][] resultImg = ConvolutionFilter.ConvolveDoubleNorm(inDataArrDbl, width, height, kernel, tgtRadius);
		
		ImageJUtility.showNewImage(resultImg, width, height, "gauss with kernel r=" + tgtRadius + " s=" + tgtSigma);
	} // run

	void showAbout() {
		IJ.showMessage("About Template_...", "Mean Mask\n");
	} // showAbout

} //class MeanMask_
