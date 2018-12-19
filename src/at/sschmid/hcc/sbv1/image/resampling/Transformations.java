package at.sschmid.hcc.sbv1.image.resampling;

import java.util.ArrayList;
import java.util.List;

public final class Transformations {
  
  private final List<TransformationItem> items = new ArrayList<>();
  
  private int pollIndex = 0;
  
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
    return pollIndex < items.size();
  }
  
  public TransformationItem next() {
    return items.get(pollIndex++);
  }
  
  public Transformations reset() {
    pollIndex = 0;
    return this;
  }
  
  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder();
    if (items.isEmpty()) {
      result.append("No transformations");
    } else {
      for (final TransformationItem item : items) {
        result.append(item);
      }
    }
    return result.toString();
  }
  
  public interface TransformationItem {
    // empty interface
  }
  
  public static class Translation implements TransformationItem {
    
    public final double x;
    public final double y;
    
    private Translation(final double x, final double y) {
      this.x = x;
      this.y = y;
    }
    
    @Override
    public String toString() {
      return String.format("Transform [%.3f, %.3f]", x, y);
    }
    
  }
  
  public static class Rotation implements TransformationItem {
    
    public final double radians;
    
    private Rotation(final double degrees) {
      this.radians = Math.toRadians(degrees);
    }
    
    @Override
    public String toString() {
      return String.format("Rotation %.3f (rad)", radians);
    }
    
  }
  
  public static class Scale implements TransformationItem {
    
    public final double factor;
    
    private Scale(final double factor) {
      this.factor = factor;
    }
    
    @Override
    public String toString() {
      return String.format("Scale %.3f", factor);
    }
    
  }
  
}
