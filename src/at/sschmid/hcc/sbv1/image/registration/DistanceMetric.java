package at.sschmid.hcc.sbv1.image.registration;

public enum DistanceMetric {
  Manhattan(new double[][]{
      { 2, 1, 2 },
      { 1, 0, 1 },
      { 2, 1, 2 }
  }),
  Euklid(new double[][]{
      { 1.41, 1, 1.41 },
      { 1.00, 0, 1.00 },
      { 1.41, 1, 1.41 }
  });
  
  public final double[][] value;
  
  DistanceMetric(final double[][] value) {
    this.value = value;
  }
}
