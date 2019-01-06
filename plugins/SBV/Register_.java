import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.SplitImage;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.registration.MatchMetric;
import at.sschmid.hcc.sbv1.image.registration.MatchMetricType;
import at.sschmid.hcc.sbv1.image.registration.Registration;
import at.sschmid.hcc.sbv1.image.resampling.Interpolation;
import at.sschmid.hcc.sbv1.image.resampling.Transformation;
import at.sschmid.hcc.sbv1.image.resampling.Transformations;
import ij.IJ;
import ij.gui.GenericDialog;

import java.util.Arrays;

public final class Register_ extends AbstractUserInputPlugIn<Register_.Input> {
  
  private static final boolean DEFAULT_SPLIT_IMAGE = true;
  private static final double DEFAULT_TRANS_X = 15;
  private static final double DEFAULT_TRANS_Y = -15;
  private static final double DEFAULT_ROTATION = 10;
  private static final boolean DEFAULT_ROTATE_FIRST = false;
  private static final MatchMetricType DEFAULT_METRIC = MatchMetricType.MI;
  private static final double DEFAULT_SEARCH_RADIUS_TRANS = 10;
  private static final double DEFAULT_SEARCH_RADIUS_ROT = DEFAULT_SEARCH_RADIUS_TRANS;
  private static final double DEFAULT_STEP_WIDTH_TRANS = 2d;
  private static final double DEFAULT_STEP_WIDTH_ROT = DEFAULT_STEP_WIDTH_TRANS;
  private static final int DEFAULT_OPTIMIZATION_RUNS = 5;
  private static final double DEFAULT_SCALE_PER_RUN = 0.9d;
  
  @Override
  public void process(final Image image) {
//    final DistanceMap distanceMap = image.distanceMap(DistanceMap.DistanceMetric.Manhattan);
    Image originalImage;
    Image transformedImage;
    if (input.splitImage) {
      final SplitImage splitImage = image.split();
      originalImage = splitImage.first().get();
      transformedImage = splitImage.last().get();
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
    dialog.addMessage("For non split images:");
    dialog.addNumericField("Translate X", DEFAULT_TRANS_X, 3);
    dialog.addNumericField("Translate Y", DEFAULT_TRANS_Y, 3);
    dialog.addNumericField("Rotation (deg)", DEFAULT_ROTATION, 3);
    dialog.addCheckbox("Rotate first", DEFAULT_ROTATE_FIRST);
  
    dialog.addMessage("Registration:");
    dialog.addChoice("Match metric",
        Arrays.stream(MatchMetricType.values()).map(Enum::toString).toArray(String[]::new),
        DEFAULT_METRIC.name);
    dialog.addNumericField("Search radius (translation)", DEFAULT_SEARCH_RADIUS_TRANS, 0);
    dialog.addNumericField("Search radius (rotation)", DEFAULT_SEARCH_RADIUS_ROT, 0);
    dialog.addNumericField("Step width (translation)", DEFAULT_STEP_WIDTH_TRANS, 3);
    dialog.addNumericField("Step width (rotation)", DEFAULT_STEP_WIDTH_ROT, 3);
    dialog.addNumericField("Max optimization runs", DEFAULT_OPTIMIZATION_RUNS, 0);
    dialog.addNumericField("Scale per run", DEFAULT_SCALE_PER_RUN, 1);
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    return new Input(dialog.getNextBoolean(),
        dialog.getNextNumber(),
        dialog.getNextNumber(),
        dialog.getNextNumber(),
        dialog.getNextBoolean(),
        MatchMetricType.values()[dialog.getNextChoiceIndex()],
        (int) dialog.getNextNumber(),
        (int) dialog.getNextNumber(),
        dialog.getNextNumber(),
        dialog.getNextNumber(),
        (int) dialog.getNextNumber(),
        dialog.getNextNumber());
  }
  
  private void registration(final Image image1, final Image image2) {
    final MatchMetric matchMetric = MatchMetric.create(input.matchMetricType, image1, image2);
    final double initialError = matchMetric.getMatch(image1, image2);
    IJ.log(String.format("Initial match = %.2f", initialError));
    image1.show(String.format("%s - image 1", pluginName));
    image2.show(String.format("%s - image 2 (e=%.2f)", pluginName, initialError));
    
    final Registration registration = Registration.create()
        .errorMetric(matchMetric)
        .stepWidthTranslation(input.stepWidthTranslation)
        .stepWidthRotation(input.stepWidthRotation)
        .searchRadiusTranslation(input.searchRadiusTranslation)
        .searchRadiusRotation(input.searchRadiusRotation)
        .maxOptimizationRuns(input.maxOptimizationRuns)
        .scalePerRun(input.scalePerRun)
        .build();
    
    final long start = System.currentTimeMillis();
    final Transformations bestTransformations = registration.register(image1, image2);
    
    IJ.log(String.format("Register Duration %.3fs%n", (System.currentTimeMillis() - start) / 1000d));
    
    if (bestTransformations == null) {
      IJ.log("Could not find a better transformation");
      return;
    }
    
    final Image registeredImage = image2.transformation()
        .transform(bestTransformations, Interpolation.Mode.BiLinear)
        .getResult();
  
    final double bestMatch = matchMetric.getMatch(image1, registeredImage);
    addResult(registeredImage, String.format("%s - registered image (e=%.2f)", pluginName, bestMatch));
    addResult(image1.calculation(registeredImage).difference(), String.format("%s - difference", pluginName));
  
    final double diff = Math.abs(bestMatch - initialError);
    final double diffRelative = diff / initialError * 100;
    IJ.log(String.format("Best match = %.2f (difference = %.2f = %.2f%%)", bestMatch, diff, diffRelative));
    IJ.log(String.format("Best transformations: %s", bestTransformations));
    IJ.log(String.format("Optimization runs: %d", registration.getOptimizationRuns()));
  }
  
  static class Input {
    
    private final boolean splitImage;
    private final double translationX;
    private final double translationY;
    private final double rotation;
    private final boolean rotateFirst;
  
    private final MatchMetricType matchMetricType;
    private final int searchRadiusTranslation;
    private final int searchRadiusRotation;
    private final double stepWidthTranslation;
    private final double stepWidthRotation;
    private final int maxOptimizationRuns;
    private final double scalePerRun;
  
    private Transformations transformations;
    
    Input(final boolean splitImage,
          final double translationX,
          final double translationY,
          final double rotation,
          final boolean rotateFirst,
          final MatchMetricType matchMetricType,
          final int searchRadiusTranslation,
          final int searchRadiusRotation,
          final double stepWidthTranslation,
          final double stepWidthRotation,
          final int maxOptimizationRuns,
          final double scalePerRun) {
      this.splitImage = splitImage;
      this.translationX = translationX;
      this.translationY = translationY;
      this.rotation = rotation;
      this.rotateFirst = rotateFirst;
      this.matchMetricType = matchMetricType;
      this.searchRadiusTranslation = searchRadiusTranslation;
      this.searchRadiusRotation = searchRadiusRotation;
      this.stepWidthTranslation = stepWidthTranslation;
      this.stepWidthRotation = stepWidthRotation;
      this.maxOptimizationRuns = maxOptimizationRuns;
      this.scalePerRun = scalePerRun;
    }
    
    @Override
    public String toString() {
      return new StringBuilder()
          .append("Register {\n  Transformation: ")
          .append(splitImage ? "split image" : getTransformations())
          .append(",\n  Registration:\n   matchMetricType=")
          .append(matchMetricType)
          .append(",\n   searchRadiusTranslation=")
          .append(searchRadiusTranslation)
          .append(",\n   searchRadiusRotation=")
          .append(searchRadiusRotation)
          .append(",\n   stepWidthTranslation=")
          .append(stepWidthTranslation)
          .append(",\n   stepWidthRotation=")
          .append(stepWidthRotation)
          .append(",\n   maxOptimizationRuns=")
          .append(maxOptimizationRuns)
          .append(",\n   scalePerRun=")
          .append(scalePerRun)
          .append("\n}")
          .toString();
    }
    
    private Transformations getTransformations() {
      if (transformations == null) {
        transformations = rotateFirst
            ? new Transformations().rotate(rotation).translate(translationX, translationY)
            : new Transformations().translate(translationX, translationY).rotate(rotation);
      }
  
      return transformations;
    }
    
  }
  
}
