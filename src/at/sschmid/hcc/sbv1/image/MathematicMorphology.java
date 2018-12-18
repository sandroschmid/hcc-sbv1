package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.utility.Point;

import java.util.Collection;
import java.util.LinkedList;

class MathematicMorphology {
  
  private final int[][] structure;
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
    this.rx = (structure.length - 1) / 2;
    this.ry = (innerLength - 1) / 2;
  }
  
  MathematicMorphology(final Neighbour neighbour) {
    this.structure = neighbour.value;
    this.rx = this.ry = neighbour.r;
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
  
  Image hitOrMiss(final Image image) {
    final Collection<Point> hits = hitOrMissPoints(image);
    final Image result = new Image(image.width, image.height);
    for (final Point point : hits) {
      result.data[point.x][point.y] = image.maxColor;
    }
    
    return result;
  }
  
  Collection<Point> hitOrMissPoints(final Image image) {
    final Collection<Point> points = new LinkedList<>();
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        boolean matching = true;
        for (int k = x - rx; k <= x + rx && matching; k++) {
          if (k >= 0 && k < image.width) {
            for (int l = y - ry; l <= y + ry && matching; l++) {
              final int s = structure[k - (x - rx)][l - (y - ry)];
              if ((s == 0 || s == 1) && l >= 0 && l < image.height) {
                if (s == 0) {
                  matching = image.data[k][l] == 0;
                } else { // s == 1
                  matching = image.data[k][l] == image.maxColor;
                }
              }
            }
          }
        }
        
        if (matching) {
          points.add(new Point(x, y));
        }
      }
    }
    
    return points;
  }
  
}
