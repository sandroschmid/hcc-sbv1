import at.sschmid.hcc.sbv1.image.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.BinaryThreshold;
import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.utility.Point;
import ij.gui.GenericDialog;
import ij.gui.PointRoi;

import java.awt.*;
import java.util.Stack;

public final class RegionGrowing_ extends AbstractUserInputPlugIn<BinaryThreshold> {

    private static final int T_MIN = 100;
    private static final int T_MAX = 255;
    private static final int BG = 0;
    private static final int FG = 255;
    private static final int UNPROCESSED_VALUE = -1;

    @Override
    protected int getSetupMask() {
        return super.getSetupMask() + ROI_REQUIRED;
    }

    @Override
    protected void process(final Image image) {
        final PointRoi roi = (PointRoi) imagePlus.getRoi();
        final Rectangle rect = roi.getBounds();
        final int[] xCoords = roi.getXCoordinates();
        final int[] yCoords = roi.getYCoordinates();
        final Point[] seeds = new Point[xCoords.length];
        for (int i = 0; i < xCoords.length; i++) {
            for (int j = 0; j < yCoords.length; j++) {
                seeds[i] = new Point(xCoords[i] + rect.x, yCoords[i] + rect.y);
            }
        }

        final Image result = performRegionGrowing(image, seeds);
        addResult(result, pluginName);
    }

    private Image performRegionGrowing(final Image image, final Point[] seeds) {
        final Image result = new Image(image.width, image.height);

        // init result
        for (int x = 0; x < result.width; x++) {
            for (int y = 0; y < result.height; y++) {
                result.data[x][y] = UNPROCESSED_VALUE;
            }
        }

        // non-recursive solution
        final Stack<Point> processingStack = new Stack<>();

        for (final Point seed : seeds) {
            // check if seed point is valid
            final int seedValue = image.data[seed.x][seed.y];
            if (seedValue >= input.thresholdMin && seedValue <= input.thresholdMax) {
                result.data[seed.x][seed.y] = input.foreground;
                processingStack.push(new Point(seed.x, seed.y));
            }

            while (!processingStack.empty()) {
                final Point nextPos = processingStack.pop();

                // check all children in n8
                for (int xOffset = -1; xOffset <= 1; xOffset++) {
                    for (int yOffset = -1; yOffset <= 1; yOffset++) {
                        final int nbX = nextPos.x + xOffset;
                        final int nbY = nextPos.y + yOffset;

                        // check if valid range
                        if (nbX >= 0 && nbX < image.width && nbY >= 0 && nbY < image.height) {
                            final int nbValue = image.data[nbX][nbY];
                            if (result.data[nbX][nbY] == UNPROCESSED_VALUE) {
                                // set colors and continue processing
                                if (nbValue >= input.thresholdMin && nbValue <= input.thresholdMax) {
                                    result.data[nbX][nbY] = input.foreground;
                                    processingStack.push(new Point(nbX, nbY));
                                } else {
                                    result.data[nbX][nbY] = input.background;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int x = 0; x < result.width; x++) {
            for (int y = 0; y < result.height; y++) {
                if (result.data[x][y] == UNPROCESSED_VALUE) {
                    result.data[x][y] = input.background;
                }
            }
        }

        return result;
    }

    @Override
    protected void setupDialog(final GenericDialog dialog) {
        dialog.addSlider("Threshold Min", 0, 255, T_MIN);
        dialog.addSlider("Threshold Max", 0, 255, T_MAX);
        dialog.addSlider("Background", 0, 255, BG);
        dialog.addSlider("Foreground", 0, 255, FG);
    }

    @Override
    protected BinaryThreshold getInput(final GenericDialog dialog) {
        return new BinaryThreshold((int) dialog.getNextNumber(),
                (int) dialog.getNextNumber(),
                (int) dialog.getNextNumber(),
                (int) dialog.getNextNumber());
    }

}
