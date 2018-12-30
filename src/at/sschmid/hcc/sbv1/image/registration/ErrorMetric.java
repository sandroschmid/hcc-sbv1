package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;

@FunctionalInterface
public interface ErrorMetric {
  
  static ErrorMetric create(final ErrorMetricType type, final Image image1, final Image image2) {
    ErrorMetric impl;
    switch (type) {
      case SSE:
        impl = new SquaredSumOfErrorMetric();
        break;
      case MI:
        impl = new MutualInformationMetric();
        break;
      case CM:
        impl = new ChamferMatchingMetric();
        break;
      default:
        throw new IllegalArgumentException("Unknown error metric type " + type);
    }
    
    impl.init(image1, image2);
    return impl;
  }
  
  default void init(final Image image1, final Image image2) {
    // nothing to do
  }
  
  default boolean isBetter(final double error, final double bestError) {
    return error < bestError;
  }
  
  double getError(final Image image1, final Image image2);
  
}
