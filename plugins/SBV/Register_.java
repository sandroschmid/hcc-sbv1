import at.sschmid.hcc.sbv1.image.*;
import at.sschmid.hcc.sbv1.utility.Utility;
import ij.gui.GenericDialog;

import java.util.concurrent.ExecutorService;

public final class Register_ extends AbstractUserInputPlugIn<Register_.Input> {
  
  private static final double DEFAULT_TRANS_X = 3.1416;
  private static final double DEFAULT_TRANS_Y = -7.9999;
  private static final double DEFAULT_ROTATION = 11.5;
  private static final int DEFAULT_OPTIMIZATION_RUNS = 5;
  
  @Override
  public void process(final Image image) {
    final Transformation transformation = new Transformation(image);
    final Image transformedImage = transformation
        .transform(input.getTransformations(), Interpolation.Mode.BiLinear)
        .getResult();
  
    final double initError = new SquaredSumOfError().getError(image, transformedImage);
    addResult(transformedImage, String.format("%s - transformed image (t,r, e=%s)", pluginName, initError));
  
    final ErrorMetric errorMetric = new SquaredSumOfError();
    final Image result = getRegisteredImage(image, transformedImage, initError, errorMetric);
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
  
  private Image getRegisteredImage(final Image image,
                                   final Image transformedImage,
                                   final double initError,
                                   final ErrorMetric errorMetric) {
    final long start = System.currentTimeMillis();
    
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
    Transformations bestTransformations = null;
    
    // offsets for further runs
    double currMidTx = 0;
    double currMidTy = 0;
    double currMidRot = 0;
    
    final int optimizationRuns = input.getOptimizationRuns();
    for (int run = 0; run < optimizationRuns; run++) {
      for (int xIdx = -searchRadius; xIdx < searchRadius; xIdx++) {
        for (int yIdx = -searchRadius; yIdx < searchRadius; yIdx++) {
          for (int rotIdx = -searchRadius; rotIdx < searchRadius; rotIdx++) {
            final double currTx = currMidTx + xIdx + stepWidthTranslation;
            final double currTy = currMidTy + yIdx + stepWidthTranslation;
            final double currRot = currMidRot + rotIdx * stepWidthRotation;
  
            final ErrorWorker[] errorWorkers = new ErrorWorker[]{
                new ErrorWorker(image,
                    transformedImage,
                    errorMetric,
                    new Transformations().translate(currTx, currTy).rotate(currRot)),
                new ErrorWorker(image,
                    transformedImage,
                    errorMetric,
                    new Transformations().rotate(currRot).translate(currTx, currTy))
            };
  
            final ExecutorService executor = Utility.threadPool();
            for (final ErrorWorker errorWorker : errorWorkers) {
              executor.execute(errorWorker);
            }
  
            Utility.wait(executor);
  
            for (final ErrorWorker errorWorker : errorWorkers) {
              if (errorWorker.error < bestSse) {
                bestSse = errorWorker.error;
                bestTx = currTx;
                bestTy = currTy;
                bestRot = currRot;
                bestTransformations = errorWorker.transformations;
//              IJ.log(String.format("new best SSE: %s, bestTz=%s, bestTy=%s, bestRot=%s",
//                  bestSse,
//                  bestTx,
//                  bestTy,
//                  bestRot));
              }
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
    
    if (bestTransformations == null) {
      return transformedImage;
    }
    
    bestTransformations.reset();
    final Image result = transformedImage.transformation(String.format("Minimal error=%s", bestSse))
        .transform(bestTransformations, Interpolation.Mode.BiLinear)
        .getResult();
  
    System.out.println(String.format("Register Duration %.3f", (System.currentTimeMillis() - start) / 1000d));
    return result;
  }
  
  @FunctionalInterface
  public interface ErrorMetric {
    
    double getError(final Image image1, final Image image2);
    
  }
  
  public static class SquaredSumOfError implements ErrorMetric {
    
    @Override
    public double getError(final Image image1, final Image image2) {
      double sseSum = 0d;
      for (int x = 0; x < image1.width; x++) {
        for (int y = 0; y < image1.height; y++) {
          final int val1 = image1.data[x][y];
          final int val2 = image2.data[x][y];
          final int diff = val1 - val2;
          sseSum += diff * diff;
        }
      }
      return sseSum;
    }
  
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
  
  private static class ErrorWorker implements Runnable {
    
    private final Image image;
    private final Image transformedImage;
    private final ErrorMetric errorMetric;
    private final Transformations transformations;
    
    private double error;
    
    private ErrorWorker(final Image image,
                        final Image transformedImage,
                        final ErrorMetric errorMetric,
                        final Transformations transformations) {
      this.image = image;
      this.transformedImage = transformedImage;
      this.errorMetric = errorMetric;
      this.transformations = transformations;
    }
    
    @Override
    public void run() {
      final Image testImage = transformedImage.transformation()
          .transform(transformations, Interpolation.Mode.NearestNeighbour)
          .getResult();
      
      error = errorMetric.getError(image, testImage);
    }
    
  }
  
}
