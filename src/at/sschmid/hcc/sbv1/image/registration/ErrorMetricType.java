package at.sschmid.hcc.sbv1.image.registration;

public enum ErrorMetricType {
  SSE("SSE"),
  MI("MI");
  
  public final String name;
  
  ErrorMetricType(final String name) {
    this.name = name;
  }
  
  @Override
  public String toString() {
    return name;
  }
}
