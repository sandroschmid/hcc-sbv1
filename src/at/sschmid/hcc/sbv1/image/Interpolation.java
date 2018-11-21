package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.utility.Point;

public final class Interpolation {
  
  private final Image image;
  
  public Interpolation(final Image image) {
    this.image = image;
  }
  
  public int getColor(final double x, final double y, final Mode mode) {
    return mode == Mode.NearestNeighbour
        ? getNearestNeighbourColor(x, y)
        : getBiLinearColor(x, y);
  }
  
  public int getNearestNeighbourColor(final double x, final double y) {
    return getRawValue(new Point((int) (x + 0.5), (int) (y + 0.5)));
  }
  
  public int getBiLinearColor(final double x, final double y) {
    // How to get the 4 coords for e.g (3.7, 12.2)
    // P0: (3,12) P1: (4,12), P2: (3, 13), P3: (4,13)
    final Point p1 = new Point((int) x, (int) y);
    final Point p2 = new Point(p1.x + 1, p1.y);
    final Point p3 = new Point(p1.x, p1.y + 1);
    final Point p4 = new Point(p1.x + 1, p1.y + 1);
    
    final double xPercentage = x - p1.x;
    final double yPercentage = y - p1.y;
    
    final int p1Color = getRawValue(p1);
    final int p2Color = getRawValue(p2);
    final int p3Color = getRawValue(p3);
    final int p4Color = getRawValue(p4);
    
    final double interpolatedColor1 = p1Color + xPercentage * (p2Color - p1Color);
    final double interpolatedColor2 = p3Color + xPercentage * (p4Color - p3Color);
    final double interpolatedColor3 = interpolatedColor1 + yPercentage * (interpolatedColor2 - interpolatedColor1);
  
    return Math.max(Math.min((int) interpolatedColor3, image.maxColor), 0);
  }
  
  private int getRawValue(final Point p) {
    return p.x >= 0 && p.x < image.width && p.y >= 0 && p.y < image.height ? image.data[p.x][p.y] : 0;
  }
  
  public enum Mode {
    NearestNeighbour,
    BiLinear
  }
  
}
