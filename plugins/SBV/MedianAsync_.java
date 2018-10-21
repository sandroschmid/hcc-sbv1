import at.sschmid.hcc.sbv1.Checkerboard;
import at.sschmid.hcc.sbv1.ImageJUtility;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.util.Arrays;
import java.util.Optional;

public class MedianAsync_ implements PlugInFilter {
  
  public int setup(String arg, ImagePlus imp) {
    if (arg.equals("about")) {
      showAbout();
      return DONE;
    }
    return DOES_8G + DOES_STACKS + SUPPORTS_MASKING;
  } // setup
  
  public void run(ImageProcessor ip) {
    getRadius().ifPresent(tgtRadius -> {
      final byte[] pixels = (byte[]) ip.getPixels();
      final int width = ip.getWidth();
      final int height = ip.getHeight();
      
      final int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);
      
      try {
        final int[][] resultImgAsync = medianFilterAsync(inDataArrInt, width, height, tgtRadius);
        ImageJUtility.showNewImage(resultImgAsync, width, height, "median image (async)");
  
        new Checkerboard(inDataArrInt, resultImgAsync, width, height)
            .generate()
            .show();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
  } // run
  
  void showAbout() {
    IJ.showMessage("About Template_...", "Async median filter\n");
  } // showAbout
  
  private Optional<Integer> getRadius() {
    GenericDialog gd = new GenericDialog("Select a radius");
    gd.addNumericField("Radius", 4, 0);
    gd.showDialog();
    
    return gd.wasCanceled() ? Optional.empty() : Optional.of((int) gd.getNextNumber());
  }
  
  private int[][] medianFilterAsync(int[][] inImg, int width, int height, int radius)
      throws InterruptedException {
    final long start = System.currentTimeMillis();
    int[][] resultImg = new int[width][height];
    
    // equally distribute the work to each available cpu-core
    final int cores = Runtime.getRuntime()
        .availableProcessors() * 2;
    final MedianFilterWorker[] workers = new MedianFilterWorker[cores];
    final Thread[] threads = new Thread[cores];
    final int workerHeight = (int) Math.floor(height / (double) cores);
    for (int i = 0; i < cores; i++) {
      final MedianFilterWorker worker = new MedianFilterWorker(inImg, width, height, i * workerHeight,
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
      for (int x = 0; x < width; x++) {
//        final int yMin = i * workerHeight;
//        for (int y = 0; y < workerHeight; y++) {
//          resultImg[x][yMin + y] = workerResult[x][y];
//        }
  
        if (workerHeight >= 0) {
          final int yMin = i * workerHeight;
          System.arraycopy(workerResult[x], 0, resultImg[x], yMin, workerHeight);
        }
      }
    }
    
    System.out.println("Median-Filter " + ((System.currentTimeMillis() - start) / 1000.0d) + "s (async)");
    
    return resultImg;
  }
  
  private class MedianFilterWorker implements Runnable {
    
    private final int[][] inImg;
    private final int width;
    private final int totalHeight;
    private final int height;
    private final int yMin;
    private final int yMax;
    private final int radius;
    
    private int[][] result;
    
    public MedianFilterWorker(int[][] inImg, int width, int totalHeight, int yMin, int yMax, int radius) {
      super();
      this.inImg = inImg;
      this.width = width;
      this.totalHeight = totalHeight;
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
                mask[++maskIdx] = inImg[nbx][nby];
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
  
} // class MedianAsync_
