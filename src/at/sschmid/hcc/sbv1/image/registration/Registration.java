package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.resampling.Transformations;
import at.sschmid.hcc.sbv1.utility.Utility;
import ij.IJ;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

public final class Registration {
  
  public static Builder create() {
    return new Builder();
  }
  
  private final MatchMetric matchMetric;
  private final int searchRadiusTranslation;
  private final int searchRadiusRotation;
  private final double stepWidthTranslation;
  private final double stepWidthRotation;
  private final int maxOptimizationRuns;
  private final double scalePerRun;
  
  private int optimizationRuns;
  
  private Registration(final MatchMetric matchMetric,
                       final int searchRadiusTranslation,
                       final int searchRadiusRotation,
                       final double stepWidthTranslation,
                       final double stepWidthRotation,
                       final int maxOptimizationRuns,
                       final double scalePerRun) {
    if (matchMetric == null
        || searchRadiusTranslation <= 0
        || searchRadiusRotation <= 0
        || stepWidthTranslation < 0
        || stepWidthRotation < 0
        || maxOptimizationRuns <= 0
        || scalePerRun <= 0
        || scalePerRun > 1) {
      throw new IllegalArgumentException();
    }
    
    this.matchMetric = matchMetric;
    this.searchRadiusTranslation = searchRadiusTranslation;
    this.searchRadiusRotation = searchRadiusRotation;
    this.stepWidthTranslation = stepWidthTranslation;
    this.stepWidthRotation = stepWidthRotation;
    this.maxOptimizationRuns = maxOptimizationRuns;
    this.scalePerRun = scalePerRun;
  }
  
  public int getOptimizationRuns() {
    return optimizationRuns;
  }
  
  public Transformations register(final Image image, final Image transformedImage) {
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
    double initialMatch = matchMetric.getMatch(image, transformedImage);
    Transformations bestTransformations = null;
    double bestMatch = initialMatch;
    
    // offsets for further runs
    double currMidTx = 0;
    double currMidTy = 0;
    double currMidRot = 0;
  
    for (optimizationRuns = 0; optimizationRuns < maxOptimizationRuns; optimizationRuns++) {
      ExecutorService threadPool = Utility.threadPool();
      final Deque<MatchWorker> matchWorkers = new LinkedList<>();
      final MatchWorker.Builder matchWorkerBuilder = MatchWorker.create()
          .withImage(image)
          .withTransformedImage(transformedImage)
          .withMatchMetric(matchMetric);
    
      for (int xIdx = -searchRadiusTranslation; xIdx < searchRadiusTranslation; xIdx++) {
        for (int yIdx = -searchRadiusTranslation; yIdx < searchRadiusTranslation; yIdx++) {
          for (int rotIdx = -searchRadiusRotation; rotIdx < searchRadiusRotation; rotIdx++) {
            final double currTx = currMidTx + xIdx * stepWidthTranslation;
            final double currTy = currMidTy + yIdx * stepWidthTranslation;
            final double currRot = currMidRot + rotIdx * stepWidthRotation;
  
            matchWorkerBuilder.withTx(currTx).withTy(currTy).withRot(currRot);
  
            if (currTx == 0 && currTy == 0 && currRot != 0) {
              final MatchWorker mwRotationOnly = matchWorkerBuilder
                  .withTransformations(new Transformations().rotate(currRot))
                  .build();
              threadPool.execute(mwRotationOnly);
              matchWorkers.addFirst(mwRotationOnly);
              
            } else if ((currTx != 0 || currTy != 0) && currRot == 0) {
              final MatchWorker mwTranslationOnly = matchWorkerBuilder
                  .withTransformations(new Transformations().translate(currTx, currTy))
                  .build();
              threadPool.execute(mwTranslationOnly);
              matchWorkers.addFirst(mwTranslationOnly);
              
            } else {
              final MatchWorker mw1 = matchWorkerBuilder
                  .withTransformations(new Transformations().translate(currTx, currTy).rotate(currRot))
                  .build();
              threadPool.execute(mw1);
              matchWorkers.addFirst(mw1);
    
              final MatchWorker mw2 = matchWorkerBuilder
                  .withTransformations(new Transformations().rotate(currRot).translate(currTx, currTy))
                  .build();
              threadPool.execute(mw2);
              matchWorkers.addFirst(mw2);
            }
          }
        }
      }
    
      Utility.join(threadPool);
    
      double newBestMatch = bestMatch;
      for (final MatchWorker matchWorker : matchWorkers) {
        final double match = matchWorker.getMatch();
        if (matchMetric.isBetter(match, newBestMatch)) {
          newBestMatch = match;
          bestTx = matchWorker.getTx();
          bestTy = matchWorker.getTy();
          bestRot = matchWorker.getRot();
          bestTransformations = matchWorker.getTransformations();
        }
      }
    
      IJ.log(String.format("Run #%d: %.5f (%s%.5f; absolute diff = %s%.5f%%)",
          optimizationRuns + 1,
          newBestMatch,
          newBestMatch - bestMatch >= 0 ? "+" : "-",
          newBestMatch - bestMatch,
          newBestMatch - bestMatch >= 0 ? "+" : "-",
          Math.abs(newBestMatch - initialMatch) / initialMatch * 100));
    
      bestMatch = newBestMatch;
      
      // prepare next run - decrease search area from global search to local search
      stepWidthTranslation *= scalePerRun;
      stepWidthRotation *= scalePerRun;
      
      currMidTx = bestTx;
      currMidTy = bestTy;
      currMidRot = bestRot;
    }
    
    return bestTransformations != null ? bestTransformations.reset() : null;
  }
  
  public static class Builder {
  
    private MatchMetric matchMetric;
    private int searchRadiusTranslation;
    private int searchRadiusRotation;
    private double stepWidthTranslation;
    private double stepWidthRotation;
    private int maxOptimizationRuns;
    private double scalePerRun;
    
    private Builder() {
      // nothing to do
    }
  
    public Builder errorMetric(final MatchMetric matchMetric) {
      this.matchMetric = matchMetric;
      return this;
    }
  
    public Builder searchRadiusTranslation(final int searchRadiusTranslation) {
      this.searchRadiusTranslation = searchRadiusTranslation;
      return this;
    }
  
    public Builder searchRadiusRotation(final int searchRadiusRotation) {
      this.searchRadiusRotation = searchRadiusRotation;
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
  
    public Builder maxOptimizationRuns(final int maxOptimizationRuns) {
      this.maxOptimizationRuns = maxOptimizationRuns;
      return this;
    }
    
    public Builder scalePerRun(final double scalePerRun) {
      this.scalePerRun = scalePerRun;
      return this;
    }
    
    public Registration build() {
      return new Registration(matchMetric,
          searchRadiusTranslation,
          searchRadiusRotation,
          stepWidthTranslation,
          stepWidthRotation,
          maxOptimizationRuns,
          scalePerRun);
    }
    
  }
  
}
