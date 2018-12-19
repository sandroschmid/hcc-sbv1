package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;

final class MutualInformationMetric implements ErrorMetric {
  
  MutualInformationMetric() {
    // nothing to do
  }
  
  @Override
  public double getError(final Image image1, final Image image2) {
    return image2.entropy() + image1.entropy() - image1.entropy2d(image2);
  }
  
}
