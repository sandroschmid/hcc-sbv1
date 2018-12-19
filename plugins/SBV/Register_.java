import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.SplitImage;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.registration.ErrorMetric;
import at.sschmid.hcc.sbv1.image.registration.ErrorMetricType;
import at.sschmid.hcc.sbv1.image.registration.Registration;
import at.sschmid.hcc.sbv1.image.resampling.Interpolation;
import at.sschmid.hcc.sbv1.image.resampling.Transformation;
import at.sschmid.hcc.sbv1.image.resampling.Transformations;
import ij.IJ;
import ij.gui.GenericDialog;

import java.util.Arrays;

public final class Register_ extends AbstractUserInputPlugIn<Register_.Input> {
  
  private static final boolean DEFAULT_SPLIT_IMAGE = false;
  private static final double DEFAULT_SEARCH_RADIUS = 10;
  private static final double DEFAULT_SCALE_PER_RUN = 0.9d;
  private static final double DEFAULT_STEP_WIDTH_TRANS = 2d;
  private static final double DEFAULT_STEP_WIDTH_ROT = DEFAULT_STEP_WIDTH_TRANS;
  private static final double DEFAULT_TRANS_X = 3.1416;
  private static final double DEFAULT_TRANS_Y = -7.9999;
  private static final double DEFAULT_ROTATION = 11.5;
  private static final int DEFAULT_OPTIMIZATION_RUNS = 5;
  private static final ErrorMetricType DEFAULT_METRIC = ErrorMetricType.MI;
  private static final boolean DEFAULT_USE_EDGES = true;
  
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
    dialog.addMessage("Registration:");
    dialog.addChoice("Error metric",
        Arrays.stream(ErrorMetricType.values()).map(Enum::toString).toArray(String[]::new),
        DEFAULT_METRIC.name);
    dialog.addNumericField("Search radius", DEFAULT_SEARCH_RADIUS, 0);
    dialog.addNumericField("Step width (translation)", DEFAULT_STEP_WIDTH_TRANS, 3);
    dialog.addNumericField("Step width (rotation)", DEFAULT_STEP_WIDTH_ROT, 3);
    dialog.addNumericField("Optimization runs", DEFAULT_OPTIMIZATION_RUNS, 0);
    dialog.addNumericField("Scale per run", DEFAULT_SCALE_PER_RUN, 1);
    dialog.addCheckbox("Use edges", DEFAULT_USE_EDGES);
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    return new Input(dialog.getNextBoolean(),
        dialog.getNextNumber(),
        dialog.getNextNumber(),
        dialog.getNextNumber(),
        ErrorMetricType.values()[dialog.getNextChoiceIndex()],
        (int) dialog.getNextNumber(),
        dialog.getNextNumber(),
        dialog.getNextNumber(),
        (int) dialog.getNextNumber(),
        dialog.getNextNumber(),
        dialog.getNextBoolean());
  }
  
  private void registration(final Image originalImage, final Image transformedImage) {
    Image image1;
    Image image2;
    if (input.useEdges) {
      image1 = originalImage.edges();
      image2 = transformedImage.edges();
    } else {
      image1 = originalImage;
      image2 = transformedImage;
    }
    
    final ErrorMetric errorMetric = ErrorMetric.create(input.errorMetricType);
    final double initialError = errorMetric.getError(image1, image2);
    addResult(image1, String.format("%s - image 1", pluginName));
    addResult(image2, String.format("%s - image 2 (e=%s)", pluginName, initialError));
    
    final Transformations bestTransformations = Registration.create()
        .errorMetric(errorMetric)
        .stepWidthTranslation(input.stepWidthTranslation)
        .stepWidthRotation(input.stepWidthRotation)
        .searchRadius(input.searchRadius)
        .optimizationRuns(input.optimizationRuns)
        .scalePerRun(input.scalePerRun)
        .build()
        .register(image1, image2);
    
    if (bestTransformations == null) {
      IJ.log("Could not find a transformation");
      return;
    }
    
    final Image registeredImage = image2.transformation()
        .transform(bestTransformations, Interpolation.Mode.BiLinear)
        .getResult();
    addResult(registeredImage, String.format("%s - registered image", pluginName));
    
    Image registeredImageNoEdges;
    if (input.useEdges) {
      registeredImageNoEdges = transformedImage.transformation()
          .transform(bestTransformations.reset(), Interpolation.Mode.BiLinear)
          .getResult();
      addResult(registeredImageNoEdges, String.format("%s - registered image (original)", pluginName));
    } else {
      registeredImageNoEdges = registeredImage;
    }
    
    addResult(originalImage.calculation(registeredImageNoEdges).difference());
  }
  
  static class Input {
    
    private final boolean splitImage;
    private final double translationX;
    private final double translationY;
    private final double rotation;
    
    private final ErrorMetricType errorMetricType;
    private final int searchRadius;
    private final double stepWidthTranslation;
    private final double stepWidthRotation;
    private final int optimizationRuns;
    private final double scalePerRun;
    private final boolean useEdges;
    
    Input(final boolean splitImage,
          final double translationX,
          final double translationY,
          final double rotation,
          final ErrorMetricType errorMetricType,
          final int searchRadius,
          final double stepWidthTranslation,
          final double stepWidthRotation,
          final int optimizationRuns,
          final double scalePerRun,
          final boolean useEdges) {
      this.splitImage = splitImage;
      this.translationX = translationX;
      this.translationY = translationY;
      this.rotation = rotation;
      this.errorMetricType = errorMetricType;
      this.searchRadius = searchRadius;
      this.stepWidthTranslation = stepWidthTranslation;
      this.stepWidthRotation = stepWidthRotation;
      this.optimizationRuns = optimizationRuns;
      this.scalePerRun = scalePerRun;
      this.useEdges = useEdges;
    }
    
    @Override
    public String toString() {
      final StringBuilder result = new StringBuilder()
          .append("Input {\n  Transformation:\n   splitImage=")
          .append(splitImage);
      
      if (!splitImage) {
        result.append(",\n   translationX=")
            .append(translationX)
            .append(",\n   translationY=")
            .append(translationY)
            .append(",\n   rotation=")
            .append(rotation);
      }
      
      return result.append(",\n  Registration:\n   errorMetricType=")
          .append(errorMetricType)
          .append(",\n   searchRadius=")
          .append(searchRadius)
          .append(",\n   stepWidthTranslation=")
          .append(stepWidthTranslation)
          .append(",\n   stepWidthRotation=")
          .append(stepWidthRotation)
          .append(",\n   optimizationRuns=")
          .append(optimizationRuns)
          .append(",\n   scalePerRun=")
          .append(scalePerRun)
          .append(",\n   useEdges=")
          .append(useEdges)
          .append("\n}")
          .toString();
    }
    
    private Transformations getTransformations() {
      return new Transformations().translate(translationX, translationY).rotate(rotation);
    }
    
  }
  
}
