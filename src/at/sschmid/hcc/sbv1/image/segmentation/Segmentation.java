package at.sschmid.hcc.sbv1.image.segmentation;

import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.utility.Point;

import java.util.Collection;

public final class Segmentation {
  
  private static final double DEFAULT_QUALITY = 1d;
  
  private final Image image;
  
  public Segmentation(final Image image) {
    this.image = image;
  }
  
  public Image regionGrowing(final Collection<Point> seeds,
                             final Neighbour neighbour,
                             final BinaryThreshold binaryThreshold) {
    return new RegionGrowing(image).regionGrowing(seeds, neighbour, binaryThreshold);
  }
  
  public Image regionLabelling(final Collection<Point> seeds,
                               final Neighbour neighbour,
                               final BinaryThreshold binaryThreshold) {
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
    return hitOrMiss(structure, DEFAULT_QUALITY);
  }
  
  public Image hitOrMiss(final int[][] structure, final double quality) {
    final Collection<Point> hits = hitOrMissPoints(structure, quality);
    final Image result = new Image(image.width, image.height);
    for (final Point point : hits) {
      result.data[point.x][point.y] = image.maxColor;
    }
    
    return result;
  }
  
  public Collection<Point> hitOrMissPoints(final int[][] structure) {
    return hitOrMissPoints(structure, DEFAULT_QUALITY);
  }
  
  public Collection<Point> hitOrMissPoints(final int[][] structure, final double quality) {
    return new MathematicMorphology(structure).hitOrMiss(image, quality);
  }
  
  public Image hitOrMissAntiAlias(final int[][] structure) {
    return hitOrMissAntiAlias(structure, DEFAULT_QUALITY);
  }
  
  public Image hitOrMissAntiAlias(final int[][] structure, final double quality) {
    final Collection<Point> hits = hitOrMissAntiAliasPoints(structure, quality);
    final Image result = new Image(image.width, image.height);
    for (final Point point : hits) {
      result.data[point.x][point.y] = image.maxColor;
    }
    
    return result;
  }
  
  public Collection<Point> hitOrMissAntiAliasPoints(final int[][] structure) {
    return hitOrMissAntiAliasPoints(structure, DEFAULT_QUALITY);
  }
  
  public Collection<Point> hitOrMissAntiAliasPoints(final int[][] structure, final double quality) {
    return new MathematicMorphology(structure).hitOrMissAntiAliased(image, quality);
  }
  
}
