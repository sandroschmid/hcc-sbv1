package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;

final class MutualInformationMetric implements ErrorMetric {
  
  private double image1Entropy;
  
  MutualInformationMetric() {
    // nothing to do
  }
  
  @Override
  public void init(final Image image1, final Image image2) {
    image1Entropy = image1.entropy();
  }
  
  @Override
  public double getError(final Image image1, final Image image2) {
    return image2.entropy() + image1Entropy - image1.entropy2d(image2);
  }
  
}
