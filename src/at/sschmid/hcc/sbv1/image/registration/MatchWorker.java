package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.resampling.Interpolation;
import at.sschmid.hcc.sbv1.image.resampling.Transformations;

final class MatchWorker implements Runnable {
  
  static Builder create() {
    return new Builder();
  }
  
  private Image image;
  private Image transformedImage;
  private MatchMetric matchMetric;
  private double tx;
  private double ty;
  private double rot;
  private Transformations transformations;
  
  private double match;
  
  private MatchWorker(final Image image,
                      final Image transformedImage,
                      final MatchMetric matchMetric,
                      final double tx,
                      final double ty,
                      final double rot,
                      final Transformations transformations) {
    this.image = image;
    this.transformedImage = transformedImage;
    this.matchMetric = matchMetric;
    this.tx = tx;
    this.ty = ty;
    this.rot = rot;
    this.transformations = transformations;
  }
  
  Transformations getTransformations() {
    return transformations;
  }
  
  double getMatch() {
    return match;
  }
  
  double getTx() {
    return tx;
  }
  
  double getTy() {
    return ty;
  }
  
  double getRot() {
    return rot;
  }
  
  @Override
  public void run() {
    Image testImage = transformedImage.transformation()
        .transform(transformations, Interpolation.Mode.NearestNeighbour)
        .getResult();
  
    match = matchMetric.getMatch(image, testImage);
  }
  
  final static class Builder {
    
    private Image image;
    private Image transformedImage;
    private MatchMetric matchMetric;
    private double tx;
    private double ty;
    private double rot;
    private Transformations transformations;
    
    Builder withImage(final Image image) {
      this.image = image;
      return this;
    }
    
    Builder withTransformedImage(final Image transformedImage) {
      this.transformedImage = transformedImage;
      return this;
    }
  
    Builder withMatchMetric(final MatchMetric matchMetric) {
      this.matchMetric = matchMetric;
      return this;
    }
    
    Builder withTx(final double tx) {
      this.tx = tx;
      return this;
    }
    
    Builder withTy(final double ty) {
      this.ty = ty;
      return this;
    }
    
    Builder withRot(final double rot) {
      this.rot = rot;
      return this;
    }
    
    Builder withTransformations(final Transformations transformations) {
      this.transformations = transformations;
      return this;
    }
  
    MatchWorker build() {
      return new MatchWorker(image, transformedImage, matchMetric, tx, ty, rot, transformations);
    }
    
  }
  
}
