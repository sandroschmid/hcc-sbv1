package at.sschmid.hcc.sbv1.image.resampling;

import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.utility.Point;

public final class Interpolation {
  
  private static final int BG_COLOR = 0;
  
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
    final Point p = new Point((int) (x + 0.5), (int) (y + 0.5));
    return p.x >= 0 && p.x < image.width && p.y >= 0 && p.y < image.height ? image.data[p.x][p.y] : BG_COLOR;
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
    
    final int p1Color = p1.x >= 0 && p1.x < image.width && p1.y >= 0 && p1.y < image.height
        ? image.data[p1.x][p1.y]
        : BG_COLOR;
    final int p2Color = p2.x >= 0 && p2.x < image.width && p2.y >= 0 && p2.y < image.height
        ? image.data[p2.x][p2.y]
        : BG_COLOR;
    final int p3Color = p3.x >= 0 && p3.x < image.width && p3.y >= 0 && p3.y < image.height
        ? image.data[p3.x][p3.y]
        : BG_COLOR;
    final int p4Color = p4.x >= 0 && p4.x < image.width && p4.y >= 0 && p4.y < image.height
        ? image.data[p4.x][p4.y]
        : BG_COLOR;
    
    final double interpolatedColor1 = p1Color + xPercentage * (p2Color - p1Color);
    final double interpolatedColor2 = p3Color + xPercentage * (p4Color - p3Color);
    final double interpolatedColor3 = interpolatedColor1 + yPercentage * (interpolatedColor2 - interpolatedColor1);
    
    return Math.max(Math.min((int) interpolatedColor3, image.maxColor), 0);
  }
  
  public enum Mode {
    NearestNeighbour,
    BiLinear
  }
  
}
