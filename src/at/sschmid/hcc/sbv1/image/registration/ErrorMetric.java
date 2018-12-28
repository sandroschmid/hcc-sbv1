package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;

@FunctionalInterface
public interface ErrorMetric {
  
  static ErrorMetric create(final ErrorMetricType type, final Image image1, final Image image2) {
    final ErrorMetric impl = ErrorMetricType.SSE.equals(type)
        ? new SquaredSumOfErrorMetric()
        : new MutualInformationMetric();
    impl.init(image1, image2);
    return impl;
  }
  
  default void init(final Image image1, final Image image2) {
    // nothing to do
  }
  
  double getError(final Image image1, final Image image2);
  
}
