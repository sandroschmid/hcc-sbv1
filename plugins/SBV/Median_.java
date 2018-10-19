import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import at.sschmid.hcc.sbv1.CSV;
import at.sschmid.hcc.sbv1.Checkerboard;
import at.sschmid.hcc.sbv1.ImageJUtility;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Median_ implements PlugInFilter {
	
//	private static final String STATS_FILE_DIR = "D:\\Documents\\Dropbox\\FH HGB\\HCC\\Semester 1\\SBV1\\UE\\UE01\\median-stats\\";
	
//	private Writer statsFile;

	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
		return DOES_8G + DOES_STACKS + SUPPORTS_MASKING;
	} // setup

	public void run(ImageProcessor ip) {
		getRadius().ifPresent(tgtRadius -> {
		
			try (final CSV csv = new CSV("median", "UE01\\files")) {
				csv.open();
			
				final byte[] pixels = (byte[]) ip.getPixels();
				final int width = ip.getWidth();
				final int height = ip.getHeight();
	
				final int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);
	
				final int[][] resultImg = medianFilter(inDataArrInt, width, height, tgtRadius, csv);
				ImageJUtility.showNewImage(resultImg, width, height, "median image");
				
				final Checkerboard checkerboard = new Checkerboard(inDataArrInt, resultImg, width, height);
				checkerboard.generate();
				checkerboard.show();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	} // run

	public void showAbout() {
		IJ.showMessage("About Template_...", "Median filter\n");
	} // showAbout
	
	private Optional<Integer> getRadius() {
		GenericDialog gd = new GenericDialog("Select a radius");
		gd.addNumericField("Radius", 4, 0);
		gd.showDialog();

		return gd.wasCanceled() ? Optional.empty() : Optional.of((int) gd.getNextNumber());
	}

	private int[][] medianFilter(int[][] inImg, int width, int height, int radius, CSV csv) throws IOException {
		final long start = System.currentTimeMillis();
		final int[][] resultImg = new int[width][height];
		final int maskWidth = 2 * radius + 1;
		final int maskSize = maskWidth * maskWidth;
		
		final String maskLabel = new StringBuilder("Distinct Mask (r=")
				.append(radius)
				.append(", w=")
				.append(maskWidth)
				.append(", s=")
				.append(maskSize)
				.append(')')
				.toString();
		
		csv.addRow(csv.row()
				.cell("Pixel")
				.cell("Old Color")
				.cell("New Color")
				.cell("Avg")
				.cell("Std-Dev")
				.cell("Min")
				.cell("Max")
				.cell("Min Delta")
				.cell("Max Delta")
				.empty()
				.cell(maskLabel));
		
		int[] mask;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int maskIdx = -1;
				mask = new int[maskSize];
				for (int xOffset = -radius; xOffset <= radius; xOffset++) {
					for (int yOffset = -radius; yOffset <= radius; yOffset++) {
						int nbx = x + xOffset;
						int nby = y + yOffset;

						if (nbx >= 0 && nbx < width && nby >= 0 && nby < height) {
							mask[++maskIdx] = inImg[nbx][nby];
						}
					}
				}

				Arrays.sort(mask);
				resultImg[x][y] = mask[maskSize / 2];
				
				final double avg = Arrays.stream(mask).average().orElse(0);
				final double sumOfAbsDiffs = Arrays.stream(mask).mapToDouble(n -> Math.pow(n - avg, 2)).sum();
				final double stdDev = Math.sqrt(sumOfAbsDiffs / (mask.length - 1));
				
				csv.addRow(csv.row()
						.cell(new StringBuilder("[").append(x).append("][").append(y).append("]").toString())
						.cell(inImg[x][y])
						.cell(resultImg[x][y])
						.floatingPointCell(avg, 3)
						.floatingPointCell(stdDev, 3)
						.cell(mask[0])
						.cell(mask[mask.length - 1])
						.cell('-')
						.cell('-')
						.empty()
						.cells(Arrays.stream(mask).distinct().toArray()));
			}
		}
		
		System.out.println("Median-Filter " + ((System.currentTimeMillis() - start) / 1000.0d) + "s");
		
		return resultImg;
	}

} // class Median_
