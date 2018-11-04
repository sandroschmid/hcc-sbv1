package at.sschmid.hcc.sbv1.image;

public final class Transformation {
  
  public final double translationX;
  public final double translationY;
  public final double rotation;
  
  public Transformation(final double translationX, final double translationY) {
    this(translationX, translationY, 0);
  }
  
  public Transformation(final double rotation) {
    this(0, 0, rotation);
  }
  
  public Transformation(final double translationX, final double translationY, final double rotation) {
    this.translationX = translationX;
    this.translationY = translationY;
    this.rotation = rotation;
  }
  
}
