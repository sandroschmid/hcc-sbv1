import at.sschmid.hcc.sbv1.image.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.utility.CSV;
import ij.gui.GenericDialog;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

public final class Median_ extends AbstractUserInputPlugIn<Integer> {
  
  private static final Logger LOGGER = Logger.getLogger(Median_.class.getName());
  private static final int defaultRadius = 4;
  
  @Override
  public void process(final Image image) {
    try (final CSV csv = new CSV("median", "UE01\\files")) {
      csv.open();
      
      final Image resultImg = medianFilter(image, input, csv);
      addResult(resultImg, pluginName);
      addResult(image.checkerboard(resultImg));
      
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private Image medianFilter(final Image image, final int radius, final CSV csv) throws IOException {
    final long start = System.currentTimeMillis();
    final Image resultImg = new Image(image.width, image.height);
    final int maskWidth = 2 * radius + 1;
    final int maskSize = maskWidth * maskWidth;
    
    final String maskLabel = new StringBuilder("Distinct Mask (r=")
        .append(radius)
        .append(", w=")
        .append(maskWidth)
        .append(", s=")
        .append(maskSize)
        .append(')')
        .toString();
    
    csv.addRow(csv.row()
        .cell("Pixel")
        .cell("Old Color")
        .cell("New Color")
        .cell("Avg")
        .cell("Std-Dev")
        .cell("Min")
        .cell("Max")
        .cell("Min Delta")
        .cell("Max Delta")
        .empty()
        .cell(maskLabel));
    
    int[] mask;
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        int maskIdx = -1;
        mask = new int[maskSize];
        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
          for (int yOffset = -radius; yOffset <= radius; yOffset++) {
            int nbx = x + xOffset;
            int nby = y + yOffset;
            
            if (nbx >= 0 && nbx < image.width && nby >= 0 && nby < image.height) {
              mask[++maskIdx] = image.data[nbx][nby];
            }
          }
        }
        
        Arrays.sort(mask);
        resultImg.data[x][y] = mask[maskIdx / 2];
        
        final double avg = Arrays.stream(mask)
            .average()
            .orElse(0);
        final double sumOfAbsDiffs = Arrays.stream(mask)
            .mapToDouble(n -> Math.pow(n - avg, 2))
            .sum();
        final double stdDev = Math.sqrt(sumOfAbsDiffs / (mask.length - 1));
        
        csv.addRow(csv.row()
            .cell(new StringBuilder("[").append(x)
                .append("][")
                .append(y)
                .append("]")
                .toString())
            .cell(image.data[x][y])
            .cell(resultImg.data[x][y])
            .floatingPointCell(avg, 3)
            .floatingPointCell(stdDev, 3)
            .cell(mask[0])
            .cell(mask[mask.length - 1])
            .cell('-')
            .cell('-')
            .empty()
            .cells(Arrays.stream(mask)
                .distinct()
                .toArray()));
      }
    }
  
    LOGGER.info(String.format("Median-Filter %ss", (System.currentTimeMillis() - start) / 1000d));
    
    return resultImg;
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Radius", defaultRadius, 0);
  }
  
  @Override
  protected Integer getInput(final GenericDialog dialog) {
    return (int) dialog.getNextNumber();
  }
  
}
