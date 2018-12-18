package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.utility.Point;

import java.util.Collection;
import java.util.LinkedList;

class MathematicMorphology {
  
  private final int[][] structure;
  private final int structureWidth;
  private final int structureHeight;
  private final double structureSize;
  private final int rx;
  private final int ry;
  
  MathematicMorphology(final int[][] structure) {
    if (structure.length % 2 == 0) {
      throw new AssertionError("Structure needs to have a hotspot");
    }
    
    if (structure.length == 0) {
      throw new AssertionError("Structure must not be empty");
    }
    
    final int innerLength = structure[0].length;
    if (innerLength % 2 == 0) {
      throw new AssertionError("Structure needs to have a hotspot");
    }
    
    for (int i = 1; i < structure.length; i++) {
      if (innerLength != structure[i].length) {
        throw new AssertionError("Inner arrays must be of equal lengths");
      }
    }
    
    this.structure = structure;
    this.structureWidth = structure.length;
    this.structureHeight = innerLength;
    this.structureSize = structureWidth * structureHeight;
    this.rx = (structure.length - 1) / 2;
    this.ry = (innerLength - 1) / 2;
  }
  
  MathematicMorphology(final Neighbour neighbour) {
    this(neighbour.value);
  }
  
  Image erosion(final Image image) {
    final Image result = new Image(image.width, image.height);
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        boolean allNeighbours = true;
        for (int k = x - rx; k <= x + rx && allNeighbours; k++) {
          if (k >= 0 && k < image.width) {
            for (int l = y - ry; l <= y + ry && allNeighbours; l++) {
              if (l >= 0 && l < image.height && structure[k - (x - rx)][l - (y - ry)] == 1) {
                allNeighbours = image.data[k][l] == 255;
              }
            }
          }
        }
        
        if (allNeighbours) {
          result.data[x][y] = image.maxColor;
        }
      }
    }
    
    return result;
  }
  
  Image dilation(final Image image) {
    final Image result = new Image(image.width, image.height);
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        boolean anyNeighbour = false;
        for (int k = x - rx; k <= x + rx && !anyNeighbour; k++) {
          if (k >= 0 && k < image.width) {
            for (int l = y - ry; l <= y + ry && !anyNeighbour; l++) {
              if (l >= 0 && l < image.height && structure[k - (x - rx)][l - (y - ry)] == 1) {
                anyNeighbour = image.data[k][l] == 255;
              }
            }
          }
        }
        
        if (anyNeighbour) {
          result.data[x][y] = image.maxColor;
        }
      }
    }
    
    return result;
  }
  
  Image hitOrMiss(final Image image, final double quality) {
    final Collection<Point> hits = hitOrMissPoints(image, quality);
    final Image result = new Image(image.width, image.height);
    for (final Point point : hits) {
      result.data[point.x][point.y] = image.maxColor;
    }
    
    return result;
  }
  
  Collection<Point> hitOrMissPoints(final Image image, final double quality) {
    final Collection<Point> points = new LinkedList<>();
    int matching;
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        matching = 0;
        for (int k = x - rx; k <= x + rx; k++) {
          if (k >= 0 && k < image.width) {
            for (int l = y - ry; l <= y + ry; l++) {
              final int s = structure[k - (x - rx)][l - (y - ry)];
              if ((s == 0 || s == 1) && l >= 0 && l < image.height) {
                if (s == 0) {
                  if (image.data[k][l] == 0) {
                    matching++;
                  }
                } else { // s == 1
                  if (image.data[k][l] == image.maxColor) {
                    matching++;
                  }
                }
              }
            }
          }
        }
  
        if (matching / structureSize >= quality) {
          points.add(new Point(x, y));
        }
      }
    }
    
    return points;
  }
  
}
