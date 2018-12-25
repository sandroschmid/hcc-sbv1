package at.sschmid.hcc.sbv1.image.segmentation;

import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.utility.Point;

import java.util.Collection;
import java.util.LinkedList;

final class MathematicMorphology {
  
  private static final double ANTI_ALIASING_THRESHOLD = 0.1d;
  
  private final int[][] structure;
  private final double structureSize;
  private final int rx;
  private final int ry;
  
  MathematicMorphology(final int[][] structure) {
    if (structure.length % 2 == 0) {
      throw new AssertionError("Structure needs to have a hotspot");
    }
  
    if (structure.length < 3) {
      throw new AssertionError("Structure must be at least of size 3x3");
    }
    
    final int innerLength = structure[0].length;
    if (innerLength % 2 == 0) {
      throw new AssertionError("Structure needs to have a hotspot");
    }
  
    if (innerLength < 3) {
      throw new AssertionError("Structure must be at least of size 3x3");
    }
    
    for (int i = 1; i < structure.length; i++) {
      if (innerLength != structure[i].length) {
        throw new AssertionError("Inner arrays must be of equal lengths");
      }
    }
    
    this.structure = structure;
    this.structureSize = structure.length * innerLength;
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
                allNeighbours = image.data[k][l] == image.maxColor;
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
                anyNeighbour = image.data[k][l] == image.maxColor;
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
  
  Collection<Point> hitOrMiss(final Image image, final double quality) {
    if (quality <= 0 || quality > 1) {
      throw new IllegalArgumentException("Quality must be a number between 0 and 1");
    }
    
    final Collection<Point> points = new LinkedList<>();
    int matching;
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        matching = 0;
        final int xEnd = x + rx;
        for (int k = x - rx; k <= xEnd; k++) {
          if (k >= 0 && k < image.width) {
            final int yEnd = y + ry;
            for (int l = y - ry; l <= yEnd; l++) {
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
  
  Collection<Point> hitOrMissAntiAliased(final Image image, final double quality) {
    if (quality <= 0 || quality > 1) {
      throw new IllegalArgumentException("Quality must be a number between 0 and 1");
    }
    
    final int[] aliasingThresholds = aliasingThresholds(image.maxColor);
    final Collection<Point> points = new LinkedList<>();
    int matching;
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        matching = 0;
        final int xEnd = x + rx;
        for (int k = x - rx; k <= xEnd; k++) {
          if (k >= 0 && k < image.width) {
            final int yEnd = y + ry;
            for (int l = y - ry; l <= yEnd; l++) {
              final int s = structure[k - (x - rx)][l - (y - ry)];
              if ((s == 0 || s == 1) && l >= 0 && l < image.height) {
                if (s == 0) {
                  if (image.data[k][l] == 0) {
                    matching++;
                  }
                } else { // s == 1
                  final int distance = Math.abs(x - k) + Math.abs(y - l);
                  if (image.data[k][l] >= aliasingThresholds[distance]) {
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
  
  /**
   * produces an array like [255, 229, 203, 180, 160, 142, 126, 112]. each color threshold is at least 1
   */
  private int[] aliasingThresholds(final int maxColor) {
    final int maxDistance = rx + ry;
    final int[] thresholds = new int[maxDistance + 1];
    double threshold = ANTI_ALIASING_THRESHOLD;
    double cumulatedThreshold = threshold;
    thresholds[0] = maxColor;
    for (int i = 1; i < thresholds.length; i++) {
      thresholds[i] = Math.max((int) ((1 - cumulatedThreshold) * thresholds[i - 1]), 1);
      threshold = Math.pow(ANTI_ALIASING_THRESHOLD, i + 1);
      cumulatedThreshold += threshold;
    }
    
    return thresholds;
  }
  
}
