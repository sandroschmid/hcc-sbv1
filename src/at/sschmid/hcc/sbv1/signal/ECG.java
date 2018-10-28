package at.sschmid.hcc.sbv1.signal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ECG {
  
  private static final String FILES_DIR = "D:\\Documents\\Dropbox\\FH HGB\\HCC\\Semester "
      + "1\\SBV1\\UE\\UE01\\data_ue1_6and1_7\\";
  
  private static final String ECG = "ECG_ohneBewegung.txt";
  private static final String ECG_MOVEMENT = "ECG_mitBewegung.txt";
  
  private static final Logger LOGGER = Logger.getLogger(at.sschmid.hcc.sbv1.signal.ECG.class.getName());
  
  private static final int VALUES_FOR_BASELINE = 100;
  private static final byte SAMPLE_WIDTH = 25;
  private static final double PEEK_CHANGE = .25d;
  
  public static void main(String[] args) {
    final List<at.sschmid.hcc.sbv1.signal.ECG> ecgs = new ArrayList<>(2);
    ecgs.add(new ECG(FILES_DIR + ECG));
    // ecgs.add(new ECG(FILES_DIR + ECG_MOVEMENT));
    ecgs.forEach(at.sschmid.hcc.sbv1.signal.ECG::process);
  }
  
  private final String fileName;
  
  public ECG(final String fileName) {
    this.fileName = fileName;
  }
  
  public void process() {
    try (final OpenSignalsTextFile ostFile = new OpenSignalsTextFile(this.fileName)) {
      ostFile.open();
      
      final List<OpenSignalsTextFile.Device> devices = ostFile.getDevices();
      for (OpenSignalsTextFile.Device device : devices) {
        LOGGER.info(String.format("Processing device '%s'", device.getName()));
  
        ostFile.read(device.getName());
        LOGGER.info(String.format("Found %d measuring points for device '%s'",
            device.getMeasuringPoints().size(),
            device.getName()));
        
//      LOGGER.info(ostFile.getDevices()
//          .get(0)
//          .getMeasuringPoints()
//          .stream()
//          .map(mp -> String.valueOf(mp[mp.length - 1]))
//          .collect(Collectors.joining("; ")));
        
        final int[] mps = predictedBaselineCorrection(device);
        LOGGER.info(Arrays.stream(mps).mapToObj(String::valueOf).collect(Collectors.joining("; ")));
      }
      
    } catch (IOException e) {
      LOGGER.severe(e.getMessage());
    }
  }
  
  private int[] predictedBaselineCorrection(final OpenSignalsTextFile.Device device) {
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
      int sample = (int) (sampleSum / (double) SAMPLE_WIDTH + .5d);
      
      if (prevSample != 0 && Math.abs((sample - prevSample) / (double) prevSample) >= PEEK_CHANGE) {
        baseline = (int) (baselineSum / (double) (i + 1) + .5d);
        LOGGER.info("Baseline " + baseline);
        break;
      } else {
        baselineSum += sampleSum;
      }
      
      prevSample = sample;
    }
    
    final int[] correctedValues = new int[mps.size()];
    for (int i = 0; i < mpsSize; i++) {
      int[] mp = mps.get(i);
      correctedValues[i] = mp[mpValueIndex] - baseline;
    }
    
    return correctedValues;
  }
  
}
