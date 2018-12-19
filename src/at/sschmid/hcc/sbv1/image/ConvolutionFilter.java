package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.utility.CSV;

import java.io.IOException;

public final class ConvolutionFilter {
  
  public static double[][] ConvolveDoubleNorm(double[][] inputImg,
                                              int width,
                                              int height,
                                              double[][] kernel,
                                              int radius,
                                              int numOfIterations) {
    double[][] returnImg = inputImg;
    for (int iterCount = 0; iterCount < numOfIterations; iterCount++) {
      returnImg = ConvolutionFilter.ConvolveDoubleNorm(returnImg, width, height, kernel, radius);
    }
    
    return returnImg;
  }
  
  public static double[][] ConvolveDoubleNorm(double[][] inputImg,
                                              int width,
                                              int height,
                                              double[][] kernel,
                                              int radius) {
    double[][] returnImg = new double[width][height];
    
    // step 1: move mask to all possible image pixel positions
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        double totalSum = .0;
        double maskCount = .0;
        
        // step 2: iterate over all mask elements (yellow in lecture slides)
        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
          for (int yOffset = -radius; yOffset <= radius; yOffset++) {
            int nbx = x + xOffset;
            int nby = y + yOffset;
            
            // step 3: check range of coordinates in convulution mask
            if (nbx >= 0 && nbx < width && nby >= 0 && nby < height) {
              totalSum += inputImg[nbx][nby] * kernel[xOffset + radius][yOffset + radius];
              maskCount += kernel[xOffset + radius][yOffset + radius];
            } // step 3
          }
        } // step 2
        
        // step 3.5: normalize
        totalSum /= maskCount;
        
        // step 4: store result to output image
        returnImg[x][y] = totalSum;
      }
    }
    
    return returnImg;
  }
  
  // unsaubere Impl am Rand, dort müsste normalisiert werden (wenn zB nur 6 Felder
  // in der Maske sind statt 9, dann wird nicht durch 9 dividiert sondern durch 6)
  public static double[][] ConvolveDouble(double[][] inputImg, int width, int height, double[][] kernel, int radius) {
    double[][] returnImg = new double[width][height];
    
    // step 1: move mask to all possible image pixel positions
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        double totalSum = .0;
        
        // step 2: iterate over all mask elements (yellow in lecture slides)
        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
          for (int yOffset = -radius; yOffset <= radius; yOffset++) {
            int nbx = x + xOffset;
            int nby = y + yOffset;
            
            // step 3: check range of coordinates in convulution mask
            if (nbx >= 0 && nbx < width && nby >= 0 && nby < height) {
              totalSum += inputImg[nbx][nby] * kernel[xOffset + radius][yOffset + radius];
            } // step 3
          }
        } // step 2
        
        // step 4: store result to output image
        returnImg[x][y] = totalSum;
      }
    }
    
    return returnImg;
  }
  
  public static double[][] GetMeanMask(int tgtRadius) {
    int size = 2 * tgtRadius + 1;
    int numOfElements = size * size;
    double maskVal = 1.0 / numOfElements;
    
    double[][] kernelImg = new double[size][size];
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        kernelImg[i][j] = maskVal;
      }
    }
    
    return kernelImg;
  }
  
  public static double[][] GetGaussMask(int radius, double sigma, boolean normalized) {
    int size = 2 * radius + 1;
    double[][] kernelImg = new double[size][size];
    
    final double sigmaPow = sigma * sigma;
    final double fixedPart = 1 / (2 * Math.PI * sigmaPow);
    final double mu = size / 2d;
    final double sigmaPowTwice = 2 * sigmaPow;
    
    try (final CSV csv = new CSV("gauss", "UE01\\files")) {
      csv.open()
          .addRow(csv.row()
              .cell("radius")
              .cell(radius))
          .addRow(csv.row()
              .cell("sigma")
              .cell(sigma))
          .addRow(csv.row()
              .cell("fixed part")
              .cell(fixedPart))
          .addRow(csv.row()
              .cell("mu")
              .cell(mu))
          .emptyRow();
      
      csvMaskTableHeader(size, mu, csv);
      
      double maskSum = 0d;
      
      double yDiff;
      double xDiff;
      for (int y = 0; y < size; y++) {
        yDiff = y - mu;
        
        final CSV.Row maskRow = csv.row()
            .cell(y)
            .cell(yDiff);
        for (int x = 0; x < size; x++) {
          xDiff = x - mu;
          final double value = fixedPart * Math.exp(-(xDiff * xDiff + yDiff * yDiff) / sigmaPowTwice);
          kernelImg[x][y] = value;
          
          maskRow.cell(value);
          maskSum += value;
        }
        
        csv.addRow(maskRow);
      }
      
      csv.addRow()
          .addRow(csv.row()
              .cell("cumulated sum value")
              .empty(2)
              .cell(maskSum));
      
      if (normalized) {
        double maxMaskValue = 0d;
        
        csv.emptyRow()
            .addRow(csv.row()
                .cell("normalized"));
        csvMaskTableHeader(size, mu, csv);
        
        double normalizedMaskSum = 0d;
        for (int y = 0; y < size; y++) {
          yDiff = y - mu;
          final CSV.Row maskRow = csv.row()
              .cell(y)
              .cell(yDiff);
          for (int x = 0; x < size; x++) {
            final double normalizedValue = kernelImg[x][y] / maskSum;
            kernelImg[x][y] = normalizedValue;
            
            maskRow.cell(normalizedValue);
            normalizedMaskSum += normalizedValue;
            
            if (normalizedValue > maxMaskValue) {
              maxMaskValue = normalizedValue;
            }
          }
          
          csv.addRow(maskRow);
        }
        
        csv.addRow()
            .addRow(csv.row()
                .cell("cumulated sum value")
                .empty(2)
                .cell(normalizedMaskSum));
        
        csv.emptyRow()
            .addRow(csv.row()
                .cell("normalized (as colors)"));
        csvMaskTableHeader(size, mu, csv);
        
        final double toColorFactor = maxMaskValue / 255.0d;
        for (int y = 0; y < size; y++) {
          yDiff = y - mu;
          final CSV.Row maskRow = csv.row()
              .cell(y)
              .cell(yDiff);
          for (int x = 0; x < size; x++) {
            final int color = (int) Math.floor(kernelImg[x][y] / toColorFactor);
            maskRow.cell(color);
          }
          
          csv.addRow(maskRow);
        }
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return kernelImg;
  }
  
  public static double[][] maskAsImage(double[][] kernel, int radius) {
    final int kernelSize = 2 * radius + 1;
    final double[][] kernelImg = new double[kernelSize][kernelSize];
    
    double kernelMax = 0d;
    for (int x = 0; x < kernelSize; x++) {
      for (int y = 0; y < kernelSize; y++) {
        final double value = kernel[x][y];
        if (value > kernelMax) {
          kernelMax = value;
        }
      }
    }
    
    // normalize to colors
    final double correctionFactor = kernelMax / 255.0d;
    for (int x = 0; x < kernelSize; x++) {
      for (int y = 0; y < kernelSize; y++) {
        kernelImg[x][y] = kernel[x][y] / correctionFactor;
      }
    }
    
    return kernelImg;
  }
  
  private static void csvMaskTableHeader(int size, final double mu, final CSV csv) throws IOException {
    final CSV.Row idxRow = csv.row()
        .cell()
        .cell("IDX");
    final CSV.Row diffRow = csv.row()
        .cell("IDX")
        .cell("DIFF");
    
    for (int x = 0; x < size; x++) {
      idxRow.cell(x);
      diffRow.cell(x - mu);
    }
    
    csv.addRow(idxRow)
        .addRow(diffRow);
  }
  
  /**
   * Summe aller Maskenelemente = 0 --> gültiger Kantendetektor. Frage: Liefert er auch Kanten bei Anwendung durch
   * Maske?
   */
  public static double[][] ApplySobelEdgeDetection(double[][] inputImg, int width, int height) {
    final double[][] returnImg = new double[width][height];
    final double[][] sobelV = new double[][] { { 1.0, 0.0, -1.0 }, { 2.0, 0.0, -2.0 }, { 1.0, 0.0, -1.0 } };
    final double[][] sobelH = new double[][] { { 1.0, 2.0, 1.0 }, { 0.0, 0.0, 0.0 }, { -1.0, -2.0, -1.0 } };
    
    final int radius = 1;
    final double[][] resultSobelV = ConvolveDouble(inputImg, width, height, sobelV, radius);
    final double[][] resultSobelH = ConvolveDouble(inputImg, width, height, sobelH, radius);
    
    double maxGradient = 1.0;
    
    // construct result
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        double vAbs = Math.abs(resultSobelV[x][y]);
        double hAbs = Math.abs(resultSobelH[x][y]);
        double resVal = vAbs + hAbs;
        returnImg[x][y] = resVal;
        
        // new max gradient?
        if (resVal > maxGradient) {
          maxGradient = resVal;
        }
      }
    }
    
    // normalized result to prevent color overflows
    double correctionFactor = maxGradient / 255.0d;
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        returnImg[x][y] /= correctionFactor;
      }
    }
    
    return returnImg;
  }
  
}
