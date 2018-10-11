import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;

import java.awt.*;

import at.sschmid.hcc.sbv1.ImageJUtility;
import at.sschmid.hcc.sbv1.ImageTransformationFilter;
import ij.gui.GenericDialog;

public class BinaryIntervalThreshold_ implements PlugInFilter {

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
		
		int tmin = 124;
		int tmax = 230;
		
		int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);

		// implement image inversion
		int[] threshTF = ImageTransformationFilter.GetBinaryThresholdTF(255, tmin, tmax, 255, 0);
		int[][] resultImg = ImageTransformationFilter.GetTransformedImage(inDataArrInt, width, height, threshTF);
		
		ImageJUtility.showNewImage(resultImg, width, height,
				"binary threshold, interval between [" + tmin + "," + tmax + "]");
	} // run

	void showAbout() {
		IJ.showMessage("About Template_...", "Binary threshold\n");
	} // showAbout

} //class Invert_
