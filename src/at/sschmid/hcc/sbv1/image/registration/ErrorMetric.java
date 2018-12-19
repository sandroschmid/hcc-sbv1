package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;

@FunctionalInterface
public interface ErrorMetric {
  
  static ErrorMetric create(final ErrorMetricType type) {
    return ErrorMetricType.SSE.equals(type) ? new SquaredSumOfErrorMetric() : new MutualInformationMetric();
  }
  
  double getError(final Image image1, final Image image2);
  
}
