package at.sschmid.hcc.sbv1.image.registration;

public enum ErrorMetricType {
  SSE("Sum of Squared Error"),
  MI("Mutual Information"),
  CM("Chamfer Matching");
  
  public final String name;
  
  ErrorMetricType(final String name) {
    this.name = name;
  }
  
  @Override
  public String toString() {
    return name;
  }
}
