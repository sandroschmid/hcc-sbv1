import at.sschmid.hcc.sbv1.image.*;
import at.sschmid.hcc.sbv1.utility.Utility;
import ij.gui.GenericDialog;

import java.util.concurrent.ExecutorService;

public final class Register_ extends AbstractUserInputPlugIn<Register_.Input> {
  
  private static final boolean DEFAULT_SPLIT_IMAGE = true;
  private static final double DEFAULT_TRANS_X = 0; // 3.1416;
  private static final double DEFAULT_TRANS_Y = 0; // -7.9999;
  private static final double DEFAULT_ROTATION = 90; // 11.5;
  private static final int DEFAULT_OPTIMIZATION_RUNS = 5;
  private static final Input.ErrorMetricType DEFAULT_METRIC = Input.ErrorMetricType.MI;
  
  @Override
  public void process(final Image image) {
    Image originalImage;
    Image transformedImage;
    if (input.splitImage) {
      final int halfWidth = image.width / 2;
      originalImage = new Image(halfWidth, image.height);
      transformedImage = new Image(halfWidth, image.height);
      for (int x = 0; x < image.width; x++) {
        for (int y = 0; y < image.height; y++) {
          if (x < halfWidth) {
            originalImage.data[x][y] = image.data[x][y];
          } else {
            transformedImage.data[x - halfWidth][y] = image.data[x][y];
          }
        }
      }
    } else {
      originalImage = image;
      final Transformation transformation = new Transformation(image);
      transformedImage = transformation
          .transform(input.getTransformations(), Interpolation.Mode.BiLinear)
          .getResult();
    }
  
    registration(originalImage, transformedImage);
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addCheckbox("Split image", DEFAULT_SPLIT_IMAGE);
    dialog.addNumericField("Translate X", DEFAULT_TRANS_X, 4);
    dialog.addNumericField("Translate Y", DEFAULT_TRANS_Y, 4);
    dialog.addNumericField("Rotation (deg)", DEFAULT_ROTATION, 4);
    dialog.addNumericField("Optimization runs", DEFAULT_OPTIMIZATION_RUNS, 0);
    dialog.addRadioButtonGroup("Error metric",
        new String[]{ Input.ErrorMetricType.SSE.value, Input.ErrorMetricType.MI.value },
        1,
        0,
        DEFAULT_METRIC.value);
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    return new Input(dialog.getNextBoolean(),
        dialog.getNextNumber(),
        dialog.getNextNumber(),
        dialog.getNextNumber(),
        (int) dialog.getNextNumber(),
        dialog.getNextRadioButton().equals(Input.ErrorMetricType.SSE.value)
            ? Input.ErrorMetricType.SSE
            : Input.ErrorMetricType.MI);
  }
  
  private void registration(final Image originalImage, final Image transformedImage) {
    final ErrorMetric errorMetric = input.errorMetricType.equals(Input.ErrorMetricType.SSE)
        ? new SquaredSumOfErrorMetric()
        : new MutualInformationMetric(originalImage, transformedImage);
    
    final double initError = errorMetric.getError(originalImage, transformedImage);
    addResult(originalImage, String.format("%s - original image", pluginName));
    addResult(transformedImage, String.format("%s - transformed image (t,r, e=%s)", pluginName, initError));
    
    final Image result = getRegisteredImage(originalImage, transformedImage, initError, errorMetric);
    addResult(result);
    addResult(originalImage.calculation(result).difference());
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
            final double currTx = currMidTx + xIdx * stepWidthTranslation; // TODO * or +?
            final double currTy = currMidTy + yIdx * stepWidthTranslation; // TODO * or +?
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
  
  public static class SquaredSumOfErrorMetric implements ErrorMetric {
    
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
  
  public static class MutualInformationMetric implements ErrorMetric {
    
    private final double sumEntropy1AndEntropy2;
    
    public MutualInformationMetric(final Image image1, final Image image2) {
      this.sumEntropy1AndEntropy2 = image2.entropy() + image1.entropy();
    }
    
    @Override
    public double getError(final Image image1, final Image image2) {
      return sumEntropy1AndEntropy2 - image1.entropy2d(image2);
    }
    
  }
  
  static class Input {
  
    private final boolean splitImage;
    private final double transX;
    private final double transY;
    private final double rotDeg;
    private final int optimizationRuns;
    private final ErrorMetricType errorMetricType;
  
    private Input(final boolean splitImage,
                  final double transX,
                  final double transY,
                  final double rotDeg,
                  final int optimizationRuns,
                  final ErrorMetricType errorMetricType) {
      this.splitImage = splitImage;
      this.transX = transX;
      this.transY = transY;
      this.rotDeg = rotDeg;
      this.optimizationRuns = optimizationRuns;
      this.errorMetricType = errorMetricType;
    }
    
    private Transformations getTransformations() {
      return new Transformations().translate(transX, transY).rotate(rotDeg);
    }
    
    private int getOptimizationRuns() {
      return optimizationRuns;
    }
  
    enum ErrorMetricType {
      SSE("SSE"),
      MI("MI");
    
      private final String value;
    
      ErrorMetricType(final String value) {
        this.value = value;
      }
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
