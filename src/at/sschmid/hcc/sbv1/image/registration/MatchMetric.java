package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;

@FunctionalInterface
public interface MatchMetric {
  
  static MatchMetric create(final MatchMetricType type, final Image image1, final Image image2) {
    MatchMetric impl;
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
        throw new IllegalArgumentException("Unknown match metric type " + type);
    }
    
    impl.init(image1, image2);
    return impl;
  }
  
  default void init(final Image image1, final Image image2) {
    // nothing to do
  }
  
  default boolean isBetter(final double match, final double bestMatch) {
    return match < bestMatch;
  }
  
  double getMatch(final Image image1, final Image image2);
  
}
