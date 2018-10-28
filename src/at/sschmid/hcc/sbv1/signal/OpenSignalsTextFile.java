package at.sschmid.hcc.sbv1.signal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class OpenSignalsTextFile implements AutoCloseable {
  
  private static final Logger LOGGER = Logger.getLogger(OpenSignalsTextFile.class.getName());
  private static final String EOH = "EndOfHeader";
  
  private final String fileName;
  private final Map<String, Device> devices = new HashMap<>();
  
  private File file;
  
  public OpenSignalsTextFile(final String fileName) {
    this.fileName = fileName;
  }
  
  public void open() throws IOException {
    file = new File(this.fileName);
    if (!file.exists() || !file.canRead()) {
      throw new IOException("File '" + fileName + "' does not exist or is not readable.");
    }
    
    parseHeader();
    LOGGER.info("Opened file: " + toString());
  }
  
  public boolean hasDevice() {
    return !devices.isEmpty();
  }
  
  public List<Device> getDevices() {
    return new ArrayList<>(devices.values());
  }
  
  public Device getDevice(final String deviceName) {
    return devices.get(deviceName);
  }
  
  public void read(final String deviceName) throws FileNotFoundException {
    final Device device = devices.get(deviceName);
    try (final Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)))) {
      String line;
      while (scanner.hasNextLine()) {
        line = scanner.nextLine();
        if (line.startsWith("#")) {
          continue;
        }
        
        final String[] lineParts = line.split(Pattern.quote("\t"));
        final int[] data = Arrays.stream(lineParts).mapToInt(Integer::parseInt).toArray();
        device.measuringPoints.add(data);
      }
    }
  }
  
  @Override
  public void close() {
    LOGGER.info("Closed '" + fileName + "'");
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName() + " {" +
        "fileName='" + fileName + '\'' +
        ", devices.size=" + devices.size() +
        '}';
  }
  
  private void parseHeader() throws FileNotFoundException {
    try (final Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)))) {
      final JsonParser parser = new JsonParser();
      String line;
      while (scanner.hasNextLine()) {
        line = scanner.nextLine();
        if (!line.startsWith("#") || EOH.equals(line)) {
          break;
        }
        
        if (line.startsWith("# {")) {
          final JsonObject object = parser.parse(line.substring(1)).getAsJsonObject();
          object.entrySet().forEach(entry -> {
            final JsonObject deviceJson = entry.getValue().getAsJsonObject();
            devices.put(entry.getKey(), Device.fromJson(deviceJson));
          });
        }
      }
    }
  }
  
  public static class Device {
    
    private static Device fromJson(final JsonObject deviceJson) {
      final JsonArray columnsJson = deviceJson.get("column").getAsJsonArray();
      final String[] columns = new String[columnsJson.size()];
      for (int i = 0; i < columns.length; i++) {
        columns[i] = columnsJson.get(i).getAsString();
      }
      
      return new Device(deviceJson.get("device name").getAsString(),
          columns,
          deviceJson.get("sampling rate").getAsInt());
    }
    
    private final String name;
    private final String[] columns;
    private final int samplingRate;
    private final List<int[]> measuringPoints;
    
    private Device(final String name,
                   final String[] columns,
                   final int samplingRate) {
      this.name = name;
      this.columns = columns;
      this.samplingRate = samplingRate;
      this.measuringPoints = new ArrayList<>();
    }
    
    @Override
    public String toString() {
      return getClass().getSimpleName() + " {" +
          "name='" + name + '\'' +
          ", columns=" + Arrays.toString(columns) +
          ", samplingRate=" + samplingRate +
          ", measuringPoints.size=" + measuringPoints.size() +
          '}';
    }
    
    public String getName() {
      return name;
    }
    
    public String[] getColumns() {
      return columns;
    }
    
    public int getSamplingRate() {
      return samplingRate;
    }
    
    public List<int[]> getMeasuringPoints() {
      return measuringPoints;
    }
    
    public void clearMeasuringPoints() {
      measuringPoints.clear();
    }
    
  }
  
}
