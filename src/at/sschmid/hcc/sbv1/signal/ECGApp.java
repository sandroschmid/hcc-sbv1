package at.sschmid.hcc.sbv1.signal;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ECGApp {
  
  private static final Logger LOGGER = Logger.getLogger(ECGApp.class.getName());
  
  private static final String FILES_DIR = "D:\\Documents\\Dropbox\\FH HGB\\HCC\\Semester "
      + "1\\SBV1\\UE\\UE01\\data_ue1_6and1_7\\";
  
  private static final String ECG = "ECG_ohneBewegung.txt";
  private static final String ECG_MOVEMENT = "ECG_mitBewegung.txt";
  
  private static final int VALUES_FOR_BASELINE = 100;
  private static final byte SAMPLE_WIDTH = 25;
  private static final double PEEK_CHANGE = .25d;
  
  private static final byte MEDIAN_FILTER_RADIUS = 15;
  
  public static void main(final String[] args) {
    new ECGApp(FILES_DIR, ECG);
  }
  
  private int mean = 0;
  private int median = 0;
  
  public ECGApp(final String filesDir, final String fileName) {
    final int[][] charts = getChartValues(filesDir + fileName);
    final int[] means = new int[charts[0].length];
    final int[] medians = new int[charts[0].length];
    for (int i = 0; i < means.length; i++) {
      means[i] = mean;
      medians[i] = median;
    }
    
    final Map<String, int[]> chartOriginal = new HashMap<>();
    chartOriginal.put("ECG", charts[0]);
    chartOriginal.put("Mean", means);
    chartOriginal.put("Median", medians);
    new LineChart(fileName + " - (original)", chartOriginal);
    
    final Map<String, int[]> chartModified = new HashMap<>();
    chartModified.put("ECG", charts[1]);
    new LineChart(fileName + " - (modified)", chartModified);
  }
  
  private int[][] getChartValues(final String fileName) {
    int[][] charts = new int[2][0];
    try (final OpenSignalsTextFile ostFile = new OpenSignalsTextFile(fileName)) {
      ostFile.open();
      
      final List<OpenSignalsTextFile.Device> devices = ostFile.getDevices();
      for (OpenSignalsTextFile.Device device : devices) {
        LOGGER.info(String.format("Processing device '%s'", device.getName()));
        ostFile.read(device.getName());
        final List<int[]> mps = device.getMeasuringPoints();
        LOGGER.info(String.format("Found %d measuring points for device '%s'", mps.size(), device.getName()));
        
        charts[0] = new int[mps.size()];
        if (mps.size() > 0) {
          int minValue = mps.get(0)[2];
          for (int i = 0; i < mps.size(); i++) {
            int value = mps.get(i)[2];
            if (value < minValue) {
              minValue = value;
            }
            charts[0][i] = value;
          }
          
          long sum = 0;
          int[] sortedValues = new int[mps.size()];
          for (int i = 0; i < mps.size(); i++) {
            final int normalizedValue = charts[0][i] - minValue;
            charts[0][i] = normalizedValue;
            sum += normalizedValue;
            sortedValues[i] = normalizedValue;
          }
          mean = (int) (sum / mps.size() + 0.5d);
          Arrays.sort(sortedValues);
          median = sortedValues[mps.size() / 2];
        }

//        charts[1] = sampleBasedBaselineCorrection(device);
//        charts[1] = medianBaselineCorrection(device);
        charts[1] = meanFilter(charts[0], MEDIAN_FILTER_RADIUS);
  
//        LOGGER.info(Arrays.stream(charts[1]).mapToObj(String::valueOf).collect(Collectors.joining("; ")));
      }
      
    } catch (IOException e) {
      LOGGER.severe(e.getMessage());
    }
    
    return charts;
  }
  
  private int[] sampleBasedBaselineCorrection(final OpenSignalsTextFile.Device device) {
    final int mpValueIndex = device.getColumns().length - 1;
    
    final List<int[]> mps = device.getMeasuringPoints();
    final int mpsSize = mps.size();
    if (mpsSize <= VALUES_FOR_BASELINE) {
      throw new RuntimeException("Not enough measuring points for a baseline correction.");
    }
    
    long baselineSum = 0;
    int baseline = 0;
    int prevSample = 0;
    for (int i = 0; i < mpsSize; i += SAMPLE_WIDTH) {
      long sampleSum = 0;
      final int sampleEnd = Math.min(i + SAMPLE_WIDTH, mpsSize);
      for (int sIdx = i; sIdx < sampleEnd; sIdx++) {
        final int mpValue = mps.get(sIdx)[mpValueIndex];
        sampleSum += mpValue;
      }
      
      final int sample = (int) (sampleSum / (double) SAMPLE_WIDTH + .5d);
      
      if (prevSample != 0 && (Math.abs(sample - prevSample) / (double) prevSample) >= PEEK_CHANGE) {
        baseline = (int) (baselineSum / (double) (i + 1) + .5d);
        LOGGER.info("Baseline " + baseline);
        break;
      } else {
        baselineSum += sampleSum;
      }
      
      prevSample = sample;
    }
    
    final int[] correctedValues = new int[mpsSize];
    for (int i = 0; i < mpsSize; i++) {
      int[] mp = mps.get(i);
      correctedValues[i] = mp[mpValueIndex] - baseline;
    }
    
    return correctedValues;
  }
  
  private int[] medianBaselineCorrection(final OpenSignalsTextFile.Device device) {
    final int mpValueIndex = device.getColumns().length - 1;
    
    final List<int[]> mps = device.getMeasuringPoints();
    final int mpsSize = mps.size();
    
    final int[] correctedValues = new int[mpsSize];
    long sum = 0;
    for (int i = 0; i < mpsSize; i++) {
      final int mpValue = mps.get(i)[mpValueIndex];
      sum += mpValue;
      correctedValues[i] = mpValue;
    }
    
    int mean = (int) (sum / (double) mpsSize + 0.5d);
    for (int i = 0; i < mpsSize; i++) {
      correctedValues[i] -= mean;
    }
    
    return correctedValues;
  }
  
  private int[] meanFilter(final int[] mps, final int radius) {
    // compute mask. maskValue = 1/((1+2r)*(1+2r))
    final int maskSize = 2 * radius + 1;
    final double maskValue = 1 / (double) maskSize;
    
    // apply mask
    final int mpsSize = mps.length;
    final int[] correctedValues = new int[mpsSize];
    for (int i = 0; i < mpsSize; i++) {
      double sum = 0d;
      double maskCount = 0d;
      for (int iOffset = -radius; iOffset <= radius; iOffset++) {
        final int ni = i + iOffset;
        if (ni >= 0 && ni < mpsSize) {
          sum += mps[ni] * maskValue;
          maskCount += maskValue;
        }
      }
      
      sum /= maskCount;
      correctedValues[i] = (int) (sum + .5d);
    }
    
    return correctedValues;
  }
  
}
