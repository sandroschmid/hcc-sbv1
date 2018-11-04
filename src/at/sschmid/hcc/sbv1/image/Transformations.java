package at.sschmid.hcc.sbv1.image;

import java.util.ArrayDeque;
import java.util.Queue;

public final class Transformations {
  
  private final Queue<TransformationItem> items = new ArrayDeque<>();
  
  public Transformations translate(final double x, final double y) {
    if (x != 0d || y != 0d) {
      items.add(new Translation(x, y));
    }
    
    return this;
  }
  
  public Transformations rotate(final double degrees) {
    if (degrees != 0) {
      items.add(new Rotation(degrees));
    }
    
    return this;
  }
  
  public Transformations scale(final double factor) {
    if (factor != 0d) {
      items.add(new Scale(factor));
    }
    
    return this;
  }
  
  public boolean hasNext() {
    return !items.isEmpty();
  }
  
  public TransformationItem next() {
    return items.poll();
  }
  
  public interface TransformationItem {
  }
  
  public static class Translation implements TransformationItem {
    
    public final double x;
    public final double y;
    
    private Translation(final double x, final double y) {
      this.x = x;
      this.y = y;
    }
    
  }
  
  public static class Rotation implements TransformationItem {
    
    public final double radians;
    
    private Rotation(final double degrees) {
      this.radians = Math.toRadians(degrees);
    }
    
  }
  
  public static class Scale implements TransformationItem {
    
    public final double factor;
    
    private Scale(final double factor) {
      this.factor = factor;
    }
    
  }
  
}
