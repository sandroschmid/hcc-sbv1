package at.sschmid.hcc.sbv1.image.registration;

public enum DistanceMetric {
  Manhattan(1, 2),
  Euklid(1, Math.sqrt(2));
  
  public final double direct;
  public final double diagonal;
  
  DistanceMetric(final double direct, final double diagonal) {
    this.direct = direct;
    this.diagonal = diagonal;
  }
}
