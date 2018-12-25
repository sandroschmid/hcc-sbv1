package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.resampling.Transformations;
import at.sschmid.hcc.sbv1.utility.Utility;
import ij.IJ;

import java.util.concurrent.ExecutorService;

public final class Registration {
  
  public static Builder create() {
    return new Builder();
  }
  
  private final ErrorMetric errorMetric;
  private final double stepWidthTranslation;
  private final double stepWidthRotation;
  private final int searchRadius;
  private final int optimizationRuns;
  private final double scalePerRun;
  
  private Registration(final ErrorMetric errorMetric,
                       final double stepWidthTranslation,
                       final double stepWidthRotation,
                       final int searchRadius,
                       final int optimizationRuns,
                       final double scalePerRun) {
    if (errorMetric == null
        || stepWidthTranslation <= 0
        || stepWidthRotation <= 0
        || searchRadius <= 0
        || optimizationRuns <= 0
        || scalePerRun <= 0
        || scalePerRun >= 1) {
      throw new IllegalArgumentException();
    }
    
    this.errorMetric = errorMetric;
    this.stepWidthTranslation = stepWidthTranslation;
    this.stepWidthRotation = stepWidthRotation;
    this.searchRadius = searchRadius;
    this.optimizationRuns = optimizationRuns;
    this.scalePerRun = scalePerRun;
  }
  
  public Transformations register(final Image image, final Image transformedImage) {
    final long start = System.currentTimeMillis();
    
    // fully automated registration:
    double stepWidthTranslation = this.stepWidthTranslation;
    double stepWidthRotation = this.stepWidthRotation;
    
    // first run
    // overall number of tested images = 21 * 21 * 21 = 9,261 Bilder (~1 Minute)
    // search space for Tx = [-20;20], namely -20, -18, -16, ..., 0, ..., 16, 18, 20 (due to step width)
    // expected result after run #1: tx=3, ty=-8, rot=12
    
    double bestTx = 0;
    double bestTy = 0;
    double bestRot = 0;
    double minError = errorMetric.getError(image, transformedImage);
    Transformations bestTransformations = null;
    
    // offsets for further runs
    double currMidTx = 0;
    double currMidTy = 0;
    double currMidRot = 0;
    
    for (int run = 0; run < optimizationRuns; run++) {
      for (int xIdx = -searchRadius; xIdx < searchRadius; xIdx++) {
        for (int yIdx = -searchRadius; yIdx < searchRadius; yIdx++) {
          for (int rotIdx = -searchRadius; rotIdx < searchRadius; rotIdx++) {
            final double currTx = currMidTx + xIdx * stepWidthTranslation;
            final double currTy = currMidTy + yIdx * stepWidthTranslation;
            final double currRot = currMidRot + rotIdx * stepWidthRotation;
            
            final ErrorWorker[] errorWorkers = new ErrorWorker[] {
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
              final double error = errorWorker.getError();
              if (error < minError) {
                minError = error;
                bestTx = currTx;
                bestTy = currTy;
                bestRot = currRot;
                bestTransformations = errorWorker.getTransformations();
//                IJ.log(String.format("Error: %s, bestTz=%s, bestTy=%s, bestRot=%s", minError, bestTx, bestTy,
//                bestRot));
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
    
    IJ.log(String.format("Minimal error: %s, transformation: %s%n",
        minError,
        bestTransformations != null ? bestTransformations.toString() : "no transformation found"));
    IJ.log(String.format("Register Duration %.3f%n", (System.currentTimeMillis() - start) / 1000d));
    
    return bestTransformations != null ? bestTransformations.reset() : null;
  }
  
  public static class Builder {
    
    private ErrorMetric errorMetric;
    private double stepWidthTranslation;
    private double stepWidthRotation;
    private int searchRadius;
    private int optimizationRuns;
    private double scalePerRun;
    
    private Builder() {
      // nothing to do
    }
    
    public Builder errorMetric(final ErrorMetric errorMetric) {
      this.errorMetric = errorMetric;
      return this;
    }
    
    public Builder stepWidthTranslation(final double stepWidthTranslation) {
      this.stepWidthTranslation = stepWidthTranslation;
      return this;
    }
    
    public Builder stepWidthRotation(final double stepWidthRotation) {
      this.stepWidthRotation = stepWidthRotation;
      return this;
    }
    
    public Builder searchRadius(final int searchRadius) {
      this.searchRadius = searchRadius;
      return this;
    }
    
    public Builder optimizationRuns(final int optimizationRuns) {
      this.optimizationRuns = optimizationRuns;
      return this;
    }
    
    public Builder scalePerRun(final double scalePerRun) {
      this.scalePerRun = scalePerRun;
      return this;
    }
    
    public Registration build() {
      return new Registration(errorMetric,
          stepWidthTranslation,
          stepWidthRotation,
          searchRadius,
          optimizationRuns,
          scalePerRun);
    }
    
  }
  
}