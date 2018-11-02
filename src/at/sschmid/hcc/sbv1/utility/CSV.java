package at.sschmid.hcc.sbv1.utility;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class for creating and writing CSV files.
 *
 * @author Sandro Schmid <sandro.schmid@students.fh-hagenberg.at>
 * @version 1.0.3
 */
public final class CSV implements AutoCloseable {
  
  private static final Logger LOG = LoggerFactory.getLogger(CSV.class.getName());
  private static final String CELL_DELIMITER = ";";
  
  /**
   * Default configuration for the CSV file. Either adopt this to your needs here for a global defaults or use {@link
   * #setDefaultConfig(Config)} before creating {@link CSV}-instances.
   */
  private static Config defaultConfig = Config.defaults();
  
  private final Config config;
  private final String filename;
  
  private Writer writer;
  
  /**
   * Initialize a new CSV-file. Use {@link #open()} to create and open the actual file before adding any lines. Not
   * opening the file first will result in an {@link IllegalStateException}.
   *
   * @param filename {@link String}
   */
  public CSV(final String filename) {
    this(filename, null);
  }
  
  /**
   * Initialize a new CSV-file. Use {@link #open()} to create and open the actual file before adding any lines. Not
   * opening the file first will result in an {@link IllegalStateException}.
   *
   * @param filename     The filename for the output file.
   * @param subDirectory The subdirectory relative to the directory defined in {@link #defaultConfig}.
   */
  public CSV(final String filename, final String subDirectory) {
    this.config = new Config(defaultConfig);
    
    final StringBuilder file = new StringBuilder(config.outputDirectory);
    if (subDirectory != null && !subDirectory.isEmpty()) {
      file.append(subDirectory)
          .append(config.directorySeparator);
    }
    
    this.filename = file.append(filename)
        .append('_')
        .append(System.currentTimeMillis())
        .append(".csv")
        .toString();
  }
  
  /**
   * Sets a default configuration for any future CSV-files. Note that previously create {@link CSV}-instances will not
   * be affected by this change.
   *
   * @param defaultConfig {@link Config}-instance for future {@link CSV}-instances.
   */
  public static void setDefaultConfig(Config defaultConfig) {
    if (defaultConfig == null) {
      throw new IllegalArgumentException("Default configuration must not be null");
    }
    
    CSV.defaultConfig = defaultConfig;
  }
  
  /**
   * Opens the CSV file.
   *
   * @return {@link CSV}-builder
   *
   * @throws FileNotFoundException if the file {@link #filename} could not be found.
   */
  public CSV open() throws IOException {
    logFileAction("Opening");
    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(this.filename)),
        StandardCharsets.UTF_8));
    return this;
  }
  
  /**
   * Create a new {@link Row}-instance.
   *
   * @return {@link Row}-instance
   */
  public Row row() {
    return new Row();
  }
  
  /**
   * Adds a row of cells with {@code byte}-values. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@code byte}-values to use for the new CSV-row
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final byte[] values) throws IOException {
    return addRow(values, 1);
  }
  
  /**
   * Adds a row of cells with {@code byte}-values {@code n} times. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@code byte}-values to use for the new CSV-row
   * @param n      Number of copies of the {@code values}
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final byte[] values, final int n) throws IOException {
    final StringBuilder row = new StringBuilder();
    for (final byte value : values) {
      row.append(value)
          .append(CELL_DELIMITER);
    }
    
    appendRow(row.deleteCharAt(row.length() - 1)
        .toString(), n);
    return this;
  }
  
  /**
   * Adds a row of cells with {@code int}-values. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@code int}-values to use for the new CSV-row
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final int[] values) throws IOException {
    return addRow(values, 1);
  }
  
  /**
   * Adds a row of cells with {@code int}-values {@code n} times. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@code int}-values to use for the new CSV-row
   * @param n      Number of copies of the {@code values}
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final int[] values, final int n) throws IOException {
    final Stream<String> stringValues = Arrays.stream(values)
        .mapToObj(Integer::toString);
    appendRow(stringValues, n);
    return this;
  }
  
  /**
   * Adds a row of cells with {@code long}-values. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@code long}-values to use for the new CSV-row
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final long[] values) throws IOException {
    return addRow(values, 1);
  }
  
  /**
   * Adds a row of cells with {@code long}-values {@code n} times. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@code long}-values to use for the new CSV-row
   * @param n      Number of copies of the {@code values}
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final long[] values, final int n) throws IOException {
    final Stream<String> stringValues = Arrays.stream(values)
        .mapToObj(Long::toString);
    appendRow(stringValues, n);
    return this;
  }
  
  /**
   * Adds a row of cells with translated {@code double}-values. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@code double}-values to use for the new CSV-row
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final double[] values) throws IOException {
    return addRow(values, 1);
  }
  
  /**
   * Adds a row of cells with translated {@code double}-values {@code n} times. Once added, a row cannot be changed
   * anymore.
   *
   * @param values Cells with {@code double}-values to use for the new CSV-row
   * @param n      Number of copies of the {@code values}
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final double[] values, final int n) throws IOException {
    final Stream<String> stringValues = Arrays.stream(values)
        .mapToObj(this::translateFloatingPoint);
    appendRow(stringValues, n);
    return this;
  }
  
  /**
   * Adds a row of cells with translated {@code float}-values. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@code float}-values to use for the new CSV-row
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final float[] values) throws IOException {
    return addRow(values, 1);
  }
  
  /**
   * Adds a row of cells with translated {@code float}-values {@code n} times. Once added, a row cannot be changed
   * anymore.
   *
   * @param values Cells with {@code float}-values to use for the new CSV-row
   * @param n      Number of copies of the {@code values}
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final float[] values, final int n) throws IOException {
    final StringBuilder row = new StringBuilder();
    for (final float value : values) {
      row.append(translateFloatingPoint(value))
          .append(CELL_DELIMITER);
    }
    
    appendRow(row.deleteCharAt(row.length() - 1)
        .toString(), n);
    return this;
  }
  
  /**
   * Adds a row of cells with {@code char}-values. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@link String}-values to use for the new CSV-row
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final char[] values) throws IOException {
    return addRow(values, 1);
  }
  
  /**
   * Adds a row of cells with {@code char}-values {@code n} times. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@link String}-values to use for the new CSV-row
   * @param n      Number of copies of the {@code values}
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final char[] values, final int n) throws IOException {
    final StringBuilder row = new StringBuilder();
    for (final char value : values) {
      row.append(value)
          .append(CELL_DELIMITER);
    }
    
    appendRow(row.deleteCharAt(row.length() - 1)
        .toString(), n);
    return this;
  }
  
  /**
   * Adds a row of cells with {@link Object}-values. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@link Object}-values to use for the new CSV-row
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final Object[] values) throws IOException {
    return addRow(values, 1);
  }
  
  /**
   * Adds a row of cells with {@link Object}-values {@code n} times. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@link Object}-values to use for the new CSV-row
   * @param n      Number of copies of the {@code values}
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final Object[] values, final int n) throws IOException {
    appendRow(Arrays.stream(values).map(Object::toString), n);
    return this;
  }
  
  /**
   * Adds a row of cells with {@link String}-values. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@link String}-values to use for the new CSV-row
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final String[] values) throws IOException {
    return addRow(values, 1);
  }
  
  /**
   * Adds a row of cells with {@link String}-values {@code n} times. Once added, a row cannot be changed anymore.
   *
   * @param values Cells with {@link String}-values to use for the new CSV-row
   * @param n      Number of copies of the {@code values}
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final String[] values, final int n) throws IOException {
    appendRow(Arrays.stream(values), n);
    return this;
  }
  
  /**
   * Gets the current cells of the {@link Row} and adds them to the file. Once added, a row cannot be changed anymore.
   *
   * @param row {@link Row}-builder to use for the new CSV-row
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final Row row) throws IOException {
    return addRow(row, 1);
  }
  
  /**
   * Gets the current cells of the {@link Row} and adds them {@code n} times to the file. Once added, a row cannot be
   * changed anymore.
   *
   * @param row {@link Row}-builder to use for the new CSV-row
   * @param n   Number of copies of the {@code values}
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow(final Row row, final int n) throws IOException {
    appendRow(row.build(), n);
    return this;
  }
  
  /**
   * Adds multiple {@link Row}s at once. Once added, a row cannot be changed anymore.
   *
   * @param rows array of {@link Row}-builders to use for the new CSV-rows
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRows(final Row[] rows) throws IOException {
    for (final Row row : rows) {
      addRow(row);
    }
    return this;
  }
  
  /**
   * Alias for {@link #emptyRow()}
   *
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV addRow() throws IOException {
    return emptyRow();
  }
  
  /**
   * Adds an empty row. Once added, a row cannot be changed anymore.
   *
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV emptyRow() throws IOException {
    return emptyRow(1);
  }
  
  /**
   * Adds {@code n} empty rows. Once added, a row cannot be changed anymore.
   *
   * @param n Number of empty rows.
   * @return {@link CSV}-builder
   *
   * @throws IOException If an I/O error occurs
   */
  public CSV emptyRow(final int n) throws IOException {
    appendRow("", n);
    return this;
  }
  
  /**
   * Saves the file.
   *
   * @return {@link CSV}-builder
   *
   * @throws IllegalStateException Thrown when the file was not opened using {@link #open()}.
   * @throws IOException           If an I/O error occurs
   */
  public CSV save() throws IOException {
    if (writer == null) {
      throw new IllegalStateException("CSV file was not opened. Cannot save file.");
    }
    
    try {
      writer.flush();
      logFileAction("Saved");
    } catch (IOException e) {
      logFileAction("Could not save");
      throw e;
    }
    
    return this;
  }
  
  /**
   * Saves and closes the CSV-file. Note that the file has to be opened again before any further rows can be added.
   *
   * @throws IOException If an I/O error occurs
   */
  @Override
  public void close() throws IOException {
    if (writer != null) {
      IOException saveException = null;
      try {
        save();
      } catch (IOException e) {
        saveException = e;
      }
      
      writer.close();
      writer = null;
      logFileAction("Closed");
      
      if (saveException != null) {
        throw saveException;
      }
    }
  }
  
  private void appendRow(final Stream<String> cells, final int n) throws IOException {
    final String row = cells.map(v -> v == null ? "" : v)
        .collect(Collectors.joining(CELL_DELIMITER));
    appendRow(row, n);
  }
  
  private void appendRow(final String row, final int n) throws IOException {
    if (writer == null) {
      throw new IllegalStateException("CSV file was not opened. Cannot add a row.");
    }
    
    for (int i = 0; i < n; i++) {
      writer.append(row)
          .append(config.linebreak);
    }
  }
  
  private void logFileAction(final String action) {
    if (config.isLoggingEnabled) {
      LOG.info(action + " file '" + this.filename + "' (" + config.lang + ")");
    }
  }
  
  private String translateFloatingPoint(double value) {
    return translateFloatingPoint(Double.toString(value));
  }
  
  private String translateFloatingPoint(float value) {
    return translateFloatingPoint(Float.toString(value));
  }
  
  private String translateFloatingPoint(String value) {
    return config.lang == Lang.ENGLISH ? value : value.replaceAll(Pattern.quote("."), ",");
  }
  
  /**
   * Language used for numeric cells.
   *
   * @author Sandro Schmid <sandro.schmid@students.fh-hagenberg.at>
   * @version 1.0.0
   */
  public enum Lang {
    ENGLISH,
    GERMAN
  }
  
  /**
   * Configuration for {@link CSV}.
   *
   * @author Sandro Schmid <sandro.schmid@students.fh-hagenberg.at>
   * @version 1.0.0
   */
  public static class Config {
    
    private Lang lang;
    private String outputDirectory;
    private String linebreak;
    private String directorySeparator;
    private boolean isLoggingEnabled;
    
    private Config() {
      // nothing to do
    }
    
    private Config(final Config template) {
      lang = template.lang;
      outputDirectory = template.outputDirectory;
      linebreak = template.linebreak;
      directorySeparator = template.directorySeparator;
      isLoggingEnabled = template.isLoggingEnabled;
    }
    
    public static Config defaults() {
      return new Config()
          .withLang(Lang.GERMAN)
          .withOutputDirectory("D:\\Documents\\Dropbox\\FH HGB\\HCC\\Semester 1\\SBV1\\UE\\")
          .withLinebreak("\r\n")
          .withDirectorySeparator("\\")
          .withLoggingEnabled(true);
    }
    
    public Lang getLang() {
      return lang;
    }
    
    public void setLang(final Lang lang) {
      this.lang = lang;
    }
    
    public Config withLang(final Lang lang) {
      setLang(lang);
      return this;
    }
    
    public String getOutputDirectory() {
      return outputDirectory;
    }
    
    public void setOutputDirectory(final String outputDirectory) {
      this.outputDirectory = outputDirectory;
    }
    
    public Config withOutputDirectory(final String outputDirectory) {
      setOutputDirectory(outputDirectory);
      return this;
    }
    
    public String getLinebreak() {
      return linebreak;
    }
    
    public void setLinebreak(final String linebreak) {
      this.linebreak = linebreak;
    }
    
    public Config withLinebreak(final String linebreak) {
      setLinebreak(linebreak);
      return this;
    }
    
    public String getDirectorySeparator() {
      return directorySeparator;
    }
    
    public void setDirectorySeparator(final String directorySeparator) {
      this.directorySeparator = directorySeparator;
    }
    
    public Config withDirectorySeparator(final String direcotrySeparator) {
      setDirectorySeparator(direcotrySeparator);
      return this;
    }
    
    public boolean isLoggingEnabled() {
      return isLoggingEnabled;
    }
    
    public void setLoggingEnabled(final boolean isLoggingEnabled) {
      this.isLoggingEnabled = isLoggingEnabled;
    }
    
    public Config withLoggingEnabled(final boolean isLoggingEnabled) {
      setLoggingEnabled(isLoggingEnabled);
      return this;
    }
    
  } // end class Config
  
  /**
   * Builder for a row in a CSV file.
   *
   * @author Sandro Schmid <sandro.schmid@students.fh-hagenberg.at>
   * @version 1.0.3
   */
  public class Row {
    
    private final StringBuilder row = new StringBuilder();
    
    /**
     * Private constructor. Use {@code row()} on a {@link CSV}-instance for creating new {@link Row}-instances.
     */
    private Row() {
      // nothing to do
    }
    
    /**
     * Adds a cell with a {@code byte}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @return {@link Row}-builder
     */
    public Row cell(final byte value) {
      return cell(value, 1);
    }
    
    /**
     * Adds a cell {@code n} times with a {@code byte}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @param n     How often the {@code value} should be added
     * @return {@link Row}-builder
     */
    public Row cell(final byte value, final int n) {
      return cell(Byte.toString(value), n);
    }
    
    /**
     * Adds a cell for each {@code byte}-value in the array. Once added, a cell cannot be changed anymore.
     *
     * @param values The values for the cells
     * @return {@link Row}-builder
     */
    public Row cells(final byte[] values) {
      for (byte value : values) {
        cell(value);
      }
      return this;
    }
    
    /**
     * Adds a cell with a {@code int}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @return {@link Row}-builder
     */
    public Row cell(final int value) {
      return cell(value, 1);
    }
    
    /**
     * Adds a cell {@code n} times with a {@code int}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @param n     How often the {@code value} should be added
     * @return {@link Row}-builder
     */
    public Row cell(final int value, final int n) {
      return cell(Integer.toString(value), n);
    }
    
    /**
     * Adds a cell for each {@code int}-value in the array. Once added, a cell cannot be changed anymore.
     *
     * @param values The values for the cells
     * @return {@link Row}-builder
     */
    public Row cells(final int[] values) {
      Arrays.stream(values)
          .forEach(this::cell);
      return this;
    }
    
    /**
     * Adds a cell with a {@code long}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @return {@link Row}-builder
     */
    public Row cell(final long value) {
      return cell(value, 1);
    }
    
    /**
     * Adds a cell {@code n} times with a {@code long}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @param n     How often the {@code value} should be added
     * @return {@link Row}-builder
     */
    public Row cell(final long value, final int n) {
      return cell(Long.toString(value), n);
    }
    
    /**
     * Adds a cell for each {@code long}-value in the array. Once added, a cell cannot be changed anymore.
     *
     * @param values The values for the cells
     * @return {@link Row}-builder
     */
    public Row cells(final long[] values) {
      Arrays.stream(values)
          .forEach(this::cell);
      return this;
    }
    
    /**
     * Adds a cell with a translated {@code double}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @return {@link Row}-builder
     */
    public Row cell(final double value) {
      return cell(value, 1);
    }
    
    /**
     * Adds a cell {@code n} times with a {@code double}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @param n     How often the {@code value} should be added
     * @return {@link Row}-builder
     */
    public Row cell(final double value, final int n) {
      return cell(translateFloatingPoint(value), n);
    }
    
    /**
     * Adds a cell for each {@code double}-value in the array. Once added, a cell cannot be changed anymore.
     *
     * @param values The values for the cells
     * @return {@link Row}-builder
     */
    public Row cells(final double[] values) {
      Arrays.stream(values)
          .forEach(this::cell);
      return this;
    }
    
    /**
     * Adds a cell with a translated {@code double}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value             The cell's value
     * @param maxFractionDigits The maximum number of fraction digits
     * @return {@link Row}-builder
     */
    public Row floatingPointCell(final double value, final int maxFractionDigits) {
      return floatingPointCell(value, maxFractionDigits, 1);
    }
    
    /**
     * Adds a cell {@code n} times with a translated {@code double}-value. Once added, a cell cannot be changed
     * anymore.
     *
     * @param value             The cell's value
     * @param maxFractionDigits The maximum number of fraction digits
     * @param n                 How often the {@code value} should be added
     * @return {@link Row}-builder
     */
    public Row floatingPointCell(final double value, final int maxFractionDigits, final int n) {
      final double formatted = new BigDecimal(value)
          .setScale(maxFractionDigits, BigDecimal.ROUND_HALF_UP)
          .doubleValue();
      return cell(formatted, n);
    }
    
    /**
     * Adds a cell for each {@code double}-value in the array. Once added, a cell cannot be changed anymore.
     *
     * @param values            The values for the cells
     * @param maxFractionDigits The maximum number of fraction digits
     * @return {@link Row}-builder
     */
    public Row floatingPointCells(final double[] values, final int maxFractionDigits) {
      Arrays.stream(values)
          .forEach(value -> floatingPointCell(value, maxFractionDigits));
      return this;
    }
    
    /**
     * Adds a cell with a translated {@code float}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @return {@link Row}-builder
     */
    public Row cell(final float value) {
      return cell(value, 1);
    }
    
    /**
     * Adds a cell {@code n} times with a {@code float}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @param n     How often the {@code value} should be added
     * @return {@link Row}-builder
     */
    public Row cell(final float value, final int n) {
      return cell(translateFloatingPoint(value), n);
    }
    
    /**
     * Adds a cell for each {@code float}-value in the array. Once added, a cell cannot be changed anymore.
     *
     * @param values The values for the cells
     * @return {@link Row}-builder
     */
    public Row cells(final float[] values) {
      for (float value : values) {
        cell(value);
      }
      return this;
    }
    
    /**
     * Adds a cell with a translated {@code float}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value             The cell's value
     * @param maxFractionDigits The maximum number of fraction digits
     * @return {@link Row}-builder
     */
    public Row floatingPointCell(final float value, final int maxFractionDigits) {
      return floatingPointCell(value, maxFractionDigits, 1);
    }
    
    /**
     * Adds a cell {@code n} times with a translated {@code float}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value             The cell's value
     * @param maxFractionDigits The maximum number of fraction digits
     * @param n                 How often the {@code value} should be added
     * @return {@link Row}-builder
     */
    public Row floatingPointCell(final float value, final int maxFractionDigits, final int n) {
      final float formatted = new BigDecimal(value)
          .setScale(maxFractionDigits, BigDecimal.ROUND_HALF_UP)
          .floatValue();
      return cell(formatted, n);
    }
    
    /**
     * Adds a cell for each {@code float}-value in the array. Once added, a cell cannot be changed anymore.
     *
     * @param values            The values for the cells
     * @param maxFractionDigits The maximum number of fraction digits
     * @return {@link Row}-builder
     */
    public Row floatingPointCells(final float[] values, final int maxFractionDigits) {
      for (float value : values) {
        floatingPointCell(value, maxFractionDigits);
      }
      return this;
    }
  
    /**
     * Adds a cell with a {@code char}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @return {@link Row}-builder
     */
    public Row cell(final char value) {
      return cell(value, 1);
    }
  
    /**
     * Adds a cell {@code n} times with a {@code char}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @param n     How often the {@code value} should be added
     * @return {@link Row}-builder
     */
    public Row cell(final char value, final int n) {
      return cell(String.valueOf(value), n);
    }
  
    /**
     * Adds a cell for each {@code char}-value in the array. Once added, a cell cannot be changed anymore.
     *
     * @param values The values for the cells
     * @return {@link Row}-builder
     */
    public Row cells(final char[] values) {
      for (float value : values) {
        cell(value);
      }
      return this;
    }
  
    /**
     * Adds a cell with a {@link Object}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @return {@link Row}-builder
     */
    public Row cell(final Object value) {
      return cell(value, 1);
    }
  
    /**
     * Adds a cell {@code n} times with a {@link Object}-value. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @param n     How often the {@code value} should be added
     * @return {@link Row}-builder
     */
    public Row cell(final Object value, final int n) {
      if (value instanceof Byte) {
        return cell((byte) value, n);
      } else if (value instanceof Integer) {
        return cell((int) value, n);
      } else if (value instanceof Long) {
        return cell((long) value, n);
      } else if (value instanceof Double) {
        return cell((double) value, n);
      } else if (value instanceof Float) {
        return cell((float) value, n);
      } else if (value instanceof Character) {
        return cell((char) value, n);
      } else {
        return cell(String.valueOf(value), n);
      }
    }
  
    /**
     * Adds a cell for each {@link Object}-value in the array. Once added, a cell cannot be changed anymore.
     *
     * @param values The values for the cells
     * @return {@link Row}-builder
     */
    public Row cells(final Object[] values) {
      Arrays.stream(values)
          .forEach(this::cell);
      return this;
    }
    
    /**
     * Adds a cell with a {@link String}-value. If {@code value} is {@code null}, an empty cell will be added. Once
     * added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @return {@link Row}-builder
     */
    public Row cell(final String value) {
      return cell(value, 1);
    }
    
    /**
     * Adds a cell {@code n} times with a {@link String}-value. If {@code value} is {@code null}, an empty cell will be
     * added. Once added, a cell cannot be changed anymore.
     *
     * @param value The cell's value
     * @param n     How often the {@code value} should be added
     * @return {@link Row}-builder
     */
    public Row cell(final String value, final int n) {
      if (value != null) {
        for (int i = 0; i < n; i++) {
          row.append(value)
              .append(CSV.CELL_DELIMITER);
        }
      } else {
        row.append(CSV.CELL_DELIMITER);
      }
      
      return this;
    }
    
    /**
     * Adds a cell for each {@link String}-value in the array. Once added, a cell cannot be changed anymore.
     *
     * @param values The values for the cells
     * @return {@link Row}-builder
     */
    public Row cells(final String[] values) {
      Arrays.stream(values)
          .forEach(this::cell);
      return this;
    }
    
    /**
     * Alias for {@link #empty()}.
     *
     * @return {@link Row}-builder
     */
    public Row cell() {
      return empty();
    }
    
    /**
     * Adds an empty cell. Once added, a cell cannot be changed anymore.
     *
     * @return {@link Row}-builder
     */
    public Row empty() {
      row.append(CSV.CELL_DELIMITER);
      return this;
    }
    
    /**
     * Adds {@code n} empty cells. Once added, a cell cannot be changed anymore.
     *
     * @param n Number of empty cells.
     * @return {@link Row}-builder.
     */
    public Row empty(final int n) {
      for (int i = 0; i < n; i++) {
        cell();
      }
      return this;
    }
    
    /**
     * Converts the previously added cells to a CSV-row.
     *
     * @return String representing a CSV-row
     */
    public String build() {
      final int lastCharIndex = row.length() - 1;
      if (lastCharIndex >= 0) {
        if (String.valueOf(row.charAt(lastCharIndex))
            .equals(CSV.CELL_DELIMITER)) {
          row.deleteCharAt(lastCharIndex);
        }
      }
      
      return row.toString();
    }
    
  } // end class RowBuilder
  
}
