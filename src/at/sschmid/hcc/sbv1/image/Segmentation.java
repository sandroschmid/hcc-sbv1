package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.utility.Point;

import java.util.Collection;

public class Segmentation {
  
  private final Image image;
  
  public Segmentation(final Image image) {
    this.image = image;
  }
  
  public Image regionGrowing(final Point[] seeds, final Neighbour neighbour, final BinaryThreshold binaryThreshold) {
    return new RegionGrowing(image).regionGrowing(seeds, neighbour, binaryThreshold);
  }
  
  public Image regionLabelling(final Point[] seeds, final Neighbour neighbour, final BinaryThreshold binaryThreshold) {
    return new RegionGrowing(image).regionLabelling(seeds, neighbour, binaryThreshold);
  }
  
  public Image erosion(final Neighbour neighbour) {
    return new MathematicMorphology(neighbour).erosion(image);
  }
  
  public Image erosion(final Neighbour neighbour, int n) {
    final MathematicMorphology mm = new MathematicMorphology(neighbour);
    Image result = image;
    while (n-- > 0) {
      result = mm.erosion(result);
    }
  
    return result;
  }
  
  public Image dilation(final Neighbour neighbour) {
    return new MathematicMorphology(neighbour).dilation(image);
  }
  
  public Image dilation(final Neighbour neighbour, int n) {
    final MathematicMorphology mm = new MathematicMorphology(neighbour);
    Image result = image;
    while (n-- > 0) {
      result = mm.dilation(result);
    }
  
    return result;
  }
  
  public Image opening(final Neighbour neighbour, final int n) {
    final MathematicMorphology mm = new MathematicMorphology(neighbour);
    Image result = image;
    int i = 0;
    while (i++ < n) {
      result = mm.erosion(result);
    }
  
    while (--i > 0) {
      result = mm.dilation(result);
    }
  
    return result;
  }
  
  public Image closing(final Neighbour neighbour, final int n) {
    final MathematicMorphology mm = new MathematicMorphology(neighbour);
    Image result = image;
    int i = 0;
    while (i++ < n) {
      result = mm.dilation(result);
    }
  
    while (--i > 0) {
      result = mm.erosion(result);
    }
  
    return result;
  }
  
  public Image hitOrMiss(final int[][] structure) {
    return new MathematicMorphology(structure).hitOrMiss(image);
  }
  
  public Image hitAndGrow(final int[][] structure,
                          final Neighbour neighbour,
                          final BinaryThreshold binaryThreshold) {
    final Collection<Point> hits = new MathematicMorphology(structure).hitOrMissPoints(image);
    final Point[] hitArr = hits.toArray(new Point[0]);
    return regionGrowing(hitArr, neighbour, binaryThreshold);
  }
  
}
