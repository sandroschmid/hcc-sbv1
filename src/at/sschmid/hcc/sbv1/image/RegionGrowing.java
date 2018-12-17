package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.utility.Point;
import ij.IJ;

import java.util.Stack;

public class RegionGrowing {
  
  private static final int UNPROCESSED_VALUE = -1;
  
  private final Image image;
  
  public RegionGrowing(final Image image) {
    this.image = image;
  }
  
  public Image regionGrowing(final Point[] seeds, final Neighbour neighbour, final BinaryThreshold binaryThreshold) {
    final Image result = createUnprocessedImage();
    
    // non-recursive solution
    final Stack<Point> processingStack = new Stack<>();
    
    for (final Point seed : seeds) {
      // check if seed point is valid
      final int seedValue = image.data[seed.x][seed.y];
      if (seedValue < binaryThreshold.thresholdMin
          || (binaryThreshold.thresholdMax != null && seedValue > binaryThreshold.thresholdMax)) {
        continue;
      }
      
      result.data[seed.x][seed.y] = binaryThreshold.foreground;
      processingStack.push(new Point(seed.x, seed.y));
      
      do {
        final Point nextPos = processingStack.pop();
        
        // check all children in n8
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
          for (int yOffset = -1; yOffset <= 1; yOffset++) {
            if (neighbour == Neighbour.N4) {
              final int offsetSum = xOffset + yOffset;
              if (!(offsetSum == -1 || offsetSum == 1)) {
                continue; // ignore diagonal neighbours in case of N4-strategy
              }
            }
            
            final int nbX = nextPos.x + xOffset;
            final int nbY = nextPos.y + yOffset;
            
            // check if valid range
            if (nbX >= 0 && nbX < image.width && nbY >= 0 && nbY < image.height) {
              final int nbValue = image.data[nbX][nbY];
              if (result.data[nbX][nbY] == UNPROCESSED_VALUE) {
                // set colors and continue processing
                if (nbValue >= binaryThreshold.thresholdMin
                    && (binaryThreshold.thresholdMax == null || nbValue <= binaryThreshold.thresholdMax)) {
                  result.data[nbX][nbY] = binaryThreshold.foreground;
                  processingStack.push(new Point(nbX, nbY));
                } else {
                  result.data[nbX][nbY] = binaryThreshold.background;
                }
              }
            }
          }
        }
      } while (!processingStack.empty());
    }
    
    finalizeResult(binaryThreshold, result);
    
    return result;
  }
  
  public Image regionLabelling(final Point[] seeds, final Neighbour neighbour, final BinaryThreshold binaryThreshold) {
    final Image result = createUnprocessedImage();
    
    // non-recursive solution
    final Stack<Point> processingStack = new Stack<>();
    
    int regionId = 255 - binaryThreshold.background;
    final int regionIdInc = binaryThreshold.foreground > binaryThreshold.background ? -1 : 1;
    IJ.log(String.format("First region id: %d (inc: %d)", regionId, regionIdInc));
    for (final Point seed : seeds) {
      // check if seed point is valid
      final int seedValue = image.data[seed.x][seed.y];
      if (seedValue < binaryThreshold.thresholdMin
          || (binaryThreshold.thresholdMax != null && seedValue > binaryThreshold.thresholdMax)) {
        continue;
      }
      
      result.data[seed.x][seed.y] = regionId;
      processingStack.push(new Point(seed.x, seed.y));
      
      do {
        final Point nextPos = processingStack.pop();
        
        // check all children in n8
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
          for (int yOffset = -1; yOffset <= 1; yOffset++) {
            if (neighbour == Neighbour.N4) {
              final int offsetSum = xOffset + yOffset;
              if (!(offsetSum == -1 || offsetSum == 1)) {
                continue; // ignore diagonal neighbours in case of N4-strategy
              }
            }
            
            final int nbX = nextPos.x + xOffset;
            final int nbY = nextPos.y + yOffset;
            
            // check if valid range
            if (nbX >= 0 && nbX < image.width && nbY >= 0 && nbY < image.height) {
              final int nbValue = image.data[nbX][nbY];
              if (result.data[nbX][nbY] == UNPROCESSED_VALUE) {
                // set colors and continue processing
                if (nbValue >= binaryThreshold.thresholdMin
                    && (binaryThreshold.thresholdMax == null || nbValue <= binaryThreshold.thresholdMax)) {
                  result.data[nbX][nbY] = regionId;
                  processingStack.push(new Point(nbX, nbY));
                } else {
                  result.data[nbX][nbY] = binaryThreshold.background;
                }
              }
            }
          }
        }
      } while (!processingStack.empty());
      
      regionId += regionIdInc;
      if (regionId == binaryThreshold.background) {
        throw new IllegalStateException("Region id is overlapping background color");
      }
      if (regionId < 0 || regionId > image.maxColor) {
        throw new IllegalStateException("Region id is out of color range");
      }
      IJ.log(String.format("Next region id: %d", regionId));
    }
    
    finalizeResult(binaryThreshold, result);
    
    return result;
  }
  
  private Image createUnprocessedImage() {
    final Image result = new Image(image.width, image.height);
    for (int x = 0; x < result.width; x++) {
      for (int y = 0; y < result.height; y++) {
        result.data[x][y] = UNPROCESSED_VALUE;
      }
    }
    
    return result;
  }
  
  private void finalizeResult(final BinaryThreshold binaryThreshold, final Image result) {
    for (int x = 0; x < result.width; x++) {
      for (int y = 0; y < result.height; y++) {
        if (result.data[x][y] == UNPROCESSED_VALUE) {
          result.data[x][y] = binaryThreshold.background;
        }
      }
    }
  }
  
}
