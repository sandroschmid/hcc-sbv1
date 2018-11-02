package at.sschmid.hcc.sbv1.signal;

import at.sschmid.hcc.sbv1.utility.LineChart;
import at.sschmid.hcc.sbv1.utility.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ECGApp {
  
  private static final Logger LOGGER;
  static {
    LoggerFactory.setLevel(Level.OFF);
    LOGGER = LoggerFactory.getLogger(ECGApp.class.getName());
  }
  
  private static final String FILES_DIR = "D:\\Documents\\Dropbox\\FH HGB\\HCC\\Semester "
      + "1\\SBV1\\UE\\UE01\\data_ue1_6and1_7\\";
  
  private static final String ECG = "ECG_ohneBewegung.txt";
  private static final String ECG_MOVEMENT = "ECG_mitBewegung.txt";
  
  private static final int VALUES_FOR_BASELINE = 100;
  private static final byte SAMPLE_WIDTH = 25;
  private static final double PEEK_CHANGE = .100d;
  
  private static final int MEDIAN_FILTER_RADIUS = 25;
  
  public static void main(final String[] args) {
    new ECGApp(FILES_DIR, ECG);
//    new ECGApp(FILES_DIR, ECG_MOVEMENT);
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
    chartOriginal.put("Peeks", charts[2]);
    new LineChart(fileName + " - (normalized original)", chartOriginal);
    
    final Map<String, int[]> chartModified = new HashMap<>();
    chartModified.put("ECG", charts[1]);
    chartModified.put("Peeks", charts[2]);
    new LineChart(fileName + " - (modified)", chartModified);
  }
  
  private int[][] getChartValues(final String fileName) {
    int[][] charts = new int[3][0];
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
        
        final int[] peekPositions = findPeekPositions(charts[0]);
//        LOGGER.info(peekPositions.length + " peeks found.");
//        for (final int peekPos : peekPositions) {
//          LOGGER.info("Peek @" + peekPos);
//        }
        
        charts[2] = peekPositions;

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
  
  private int[] findPeekPositions(final int[] mps) {
    final int[] peekPositions = new int[mps.length];
    
    int prevSample = -1;
    boolean peekFound = false;
    for (int i = 0; i < mps.length; i += SAMPLE_WIDTH) {
      peekPositions[i] = 0;
      
      double sampleSum = .0d;
      for (int j = i; j < Math.min(i + SAMPLE_WIDTH, mps.length); j++) {
        sampleSum += mps[j];
      }
      
      final int sample = (int) (sampleSum / (double) mps.length + .5d);
      
      if (prevSample > 0 && sample > mean) {
        final int sampleDiff = sample - prevSample;
        final double incRatio = sampleDiff / (double) prevSample;
        if (Math.abs(incRatio) >= PEEK_CHANGE) {
          if (sampleDiff > 0) { // only positive peeks
            if (!peekFound) {
//              peekPositions[i + SAMPLE_WIDTH / 2] = 1000;
              LOGGER.info("Peek at " + i);
              peekFound = true;
            }
          } else {
            peekFound = false;
          }
        }
        
        if (peekFound) {
          peekPositions[i] = mean / 2;
        }

//        if (sampleDiff > 0) { // only positive peeks
//          if (!peekFound) {
//            if (incRatio >= PEEK_CHANGE) {
//              peekPositions.add(i + SAMPLE_WIDTH);
//              peekFound = true;
//            }
//          }
//        } else {
//          peekFound = false;
//        }
      }
      
      prevSample = sample;
    }
    
    return peekPositions; // .stream().mapToInt(i -> i).toArray();
  }
  
}
