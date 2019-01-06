package at.sschmid.hcc.sbv1.utility;

public final class Point {
  
  public final int x;
  public final int y;
  
  public Point(final int x, final int y) {
    this.x = x;
    this.y = y;
  }
  
  @Override
  public String toString() {
    return String.format("[%d;%d]", x, y);
  }
  
}
