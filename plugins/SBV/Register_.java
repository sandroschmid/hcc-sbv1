import at.sschmid.hcc.sbv1.image.*;
import ij.IJ;
import ij.gui.GenericDialog;

public final class Register_ extends AbstractUserInputPlugIn<Register_.Input> {
  
  private static final Interpolation.Mode INTERPOLATION_MODE = Interpolation.Mode.BiLinear;
  private static final double DEFAULT_TRANS_X = 3.1416;
  private static final double DEFAULT_TRANS_Y = -7.9999;
  private static final double DEFAULT_ROTATION = 11.5;
  private static final int DEFAULT_OPTIMIZATION_RUNS = 1;
  
  @Override
  public void process(final Image image) {
    final Transformation transformation = new Transformation(image);
    final Image transformedImage = transformation
        .transform(input.getTransformations(), INTERPOLATION_MODE)
        .getResult();
  
    final double initError = getSSEMetricError(image, transformedImage);
    addResult(transformedImage, String.format("%s - transformed image (t,r, e=%s)", pluginName, initError));
  
    final Image result = getRegisteredImage(image, transformedImage, initError);
    addResult(result);
    addResult(image.calculation(result).difference());
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Translate X", DEFAULT_TRANS_X, 4);
    dialog.addNumericField("Translate Y", DEFAULT_TRANS_Y, 4);
    dialog.addNumericField("Rotation (deg)", DEFAULT_ROTATION, 4);
    dialog.addNumericField("Optimization runs", DEFAULT_OPTIMIZATION_RUNS, 0);
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    return new Input(dialog.getNextNumber(),
        dialog.getNextNumber(),
        dialog.getNextNumber(),
        (int) dialog.getNextNumber());
  }
  
  private Image getRegisteredImage(final Image image, final Image transformedImage, final double initError) {
    // fully automated registration using the following parameters:
    final int searchRadius = 10; // 10 to left, 10 to right and mid --> 21
    final double scalePerRun = 0.9d;
    double stepWidthTranslation = 2d;
    double stepWidthRotation = 2d;
    
    // first run
    // overall number of tested images = 21 * 21 * 21 = 9,261 Bilder (~1 Minute)
    // search space for Tx = [-20;20], namely -20, -18, -16, ..., 0, ..., 16, 18, 20 (due to step width)
    // expected result after run #1: tx=3, ty=-8, rot=12
    
    double bestTx = 0;
    double bestTy = 0;
    double bestRot = 0;
    double bestSse = initError;
    
    // offsets for further runs
    double currMidTx = 0;
    double currMidTy = 0;
    double currMidRot = 0;
    
    for (int run = 0; run < input.getOptimizationRuns(); run++) {
      for (int xIdx = -searchRadius; xIdx < searchRadius; xIdx++) {
        for (int yIdx = -searchRadius; yIdx < searchRadius; yIdx++) {
          for (int rotIdx = -searchRadius; rotIdx < searchRadius; rotIdx++) {
            double currTx = currMidTx + xIdx + stepWidthTranslation;
            double currTy = currMidTy + yIdx + stepWidthTranslation;
            double currRot = currMidRot + rotIdx * stepWidthRotation;
            final Transformations transformations = new Transformations().translate(currTx, currTy).rotate(currRot);
            final Image testImage = transformedImage.transformation().transform(transformations).getResult();
            double currErr = getSSEMetricError(image, testImage);
            if (currErr < bestSse) {
              bestSse = currErr;
              bestTx = currTx;
              bestTy = currTy;
              bestRot = currRot;
              IJ.log(String.format("new best SSE: %s, bestTz=%s, bestTy=%s, bestRot=%s",
                  bestSse,
                  bestTx,
                  bestTy,
                  bestRot));
            }
          }
        }
      }
      
      // prepare next run
      // decrease search area from global search to local search
      stepWidthTranslation *= scalePerRun;
      stepWidthRotation *= scalePerRun;
      
      currMidTx = bestTx;
      currMidTy = bestTy;
      currMidRot = bestRot;
    }
    
    final Transformations transformations = new Transformations().translate(bestTx, bestTy).rotate(bestRot);
    return transformedImage.transformation(String.format("minimal error=%s", bestSse))
        .transform(transformations)
        .getResult();
  }
  
  private double getSSEMetricError(final Image img1, Image img2) {
    double sseSum = 0d;
    for (int x = 0; x < img1.width; x++) {
      for (int y = 0; y < img1.height; y++) {
        final int val1 = img1.data[x][y];
        final int val2 = img2.data[x][y];
        final int diff = val1 - val2;
        sseSum += diff * diff;
      }
    }
    return sseSum;
  }
  
  static class Input {
    
    private final double transX;
    private final double transY;
    private final double rotDeg;
    private final int optimizationRuns;
    
    private Input(final double transX,
                  final double transY,
                  final double rotDeg, final int optimizationRuns) {
      this.transX = transX;
      this.transY = transY;
      this.rotDeg = rotDeg;
      this.optimizationRuns = optimizationRuns;
    }
    
    private Transformations getTransformations() {
      return new Transformations().translate(transX, transY).rotate(rotDeg);
    }
    
    private int getOptimizationRuns() {
      return optimizationRuns;
    }
    
  }
  
}
