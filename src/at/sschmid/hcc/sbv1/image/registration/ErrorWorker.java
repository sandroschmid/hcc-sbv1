package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.resampling.Interpolation;
import at.sschmid.hcc.sbv1.image.resampling.Transformations;

final class ErrorWorker implements Runnable {
  
  private final Image image;
  private final Image transformedImage;
  private final ErrorMetric errorMetric;
  private final Transformations transformations;
  
  private double error;
  
  ErrorWorker(final Image image,
              final Image transformedImage,
              final ErrorMetric errorMetric,
              final Transformations transformations) {
    this.image = image;
    this.transformedImage = transformedImage;
    this.errorMetric = errorMetric;
    this.transformations = transformations;
  }
  
  public Transformations getTransformations() {
    return transformations;
  }
  
  public double getError() {
    return error;
  }
  
  @Override
  public void run() {
    final Image testImage = transformedImage.transformation()
        .transform(transformations, Interpolation.Mode.NearestNeighbour)
        .getResult();
    
    error = errorMetric.getError(image, testImage);
  }
  
}
