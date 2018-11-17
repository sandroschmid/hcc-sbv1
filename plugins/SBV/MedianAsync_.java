import at.sschmid.hcc.sbv1.image.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.utility.Utility;
import ij.gui.GenericDialog;

import java.util.Arrays;
import java.util.logging.Logger;

public final class MedianAsync_ extends AbstractUserInputPlugIn<Integer> {
  
  private static final Logger LOGGER = Logger.getLogger(MedianAsync_.class.getName());
  private static final int defaultRadius = 4;
  
  @Override
  public void process(final Image image) {
    try {
      final Image resultImg = medianFilterAsync(image, input);
      resultImg.withName(pluginName).show();
      image.checkerboard(resultImg).show();
      
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Radius", defaultRadius, 0);
  }
  
  @Override
  protected Integer getInput(final GenericDialog dialog) {
    return (int) dialog.getNextNumber();
  }
  
  private Image medianFilterAsync(final Image image, final int radius)
      throws InterruptedException {
    final long start = System.currentTimeMillis();
    final Image resultImg = new Image(image.width, image.height);
    
    // equally distribute the work to each available cpu-core
    final int cores = Utility.threadCount();
    final MedianFilterWorker[] workers = new MedianFilterWorker[cores];
    final Thread[] threads = new Thread[cores];
    final int workerHeight = (int) Math.floor(image.height / (double) cores);
    for (int i = 0; i < cores; i++) {
      final MedianFilterWorker worker = new MedianFilterWorker(image, i * workerHeight,
          i * workerHeight + workerHeight, radius);
      workers[i] = worker;
      
      final Thread thread = new Thread(worker);
      thread.start();
      threads[i] = thread;
    }
    
    // wait for all worker threads to finish
    for (final Thread thread : threads) {
      thread.join();
    }
    
    // merge partial results
    for (int i = 0; i < workers.length; i++) {
      final int[][] workerResult = workers[i].result;
      for (int x = 0; x < image.width; x++) {
//        final int yMin = i * workerHeight;
//        for (int y = 0; y < workerHeight; y++) {
//          resultImg[x][yMin + y] = workerResult[x][y];
//        }
        
        if (workerHeight >= 0) {
          final int yMin = i * workerHeight;
          System.arraycopy(workerResult[x], 0, resultImg.data[x], yMin, workerHeight);
        }
      }
    }
    
    LOGGER.info("Median-Filter " + ((System.currentTimeMillis() - start) / 1000.0d) + "s (async)");
    
    return resultImg;
  }
  
  private class MedianFilterWorker implements Runnable {
    
    private final int[][] image;
    private final int width;
    private final int totalHeight;
    private final int height;
    private final int yMin;
    private final int yMax;
    private final int radius;
    
    private int[][] result;
    
    public MedianFilterWorker(final Image image, int yMin, int yMax, int radius) {
      this.image = image.data;
      this.width = image.width;
      this.totalHeight = image.height;
      this.yMin = yMin;
      this.yMax = yMax;
      this.radius = radius;
      this.height = yMax - yMin;
    }
    
    @Override
    public void run() {
      result = null;
      final int[][] resultImg = new int[width][height];
      final int maskWidth = 2 * radius + 1;
      final int maskSize = maskWidth * maskWidth;
      
      int[] mask;
      for (int x = 0; x < width; x++) {
        for (int y = yMin; y < yMax; y++) {
          int maskIdx = -1;
          mask = new int[maskSize];
          for (int xOffset = -radius; xOffset <= radius; xOffset++) {
            for (int yOffset = -radius; yOffset <= radius; yOffset++) {
              int nbx = x + xOffset;
              int nby = y + yOffset;
              
              if (nbx >= 0 && nbx < width && nby >= 0 && nby < totalHeight) {
                mask[++maskIdx] = image[nbx][nby];
              }
            }
          }
          
          Arrays.sort(mask);
          resultImg[x][y - yMin] = mask[maskIdx / 2];
        }
      }
      
      result = resultImg;
    }
    
  }
  
}
