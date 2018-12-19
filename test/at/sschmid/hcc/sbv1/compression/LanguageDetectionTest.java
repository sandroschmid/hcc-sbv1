package at.sschmid.hcc.sbv1.compression;

import at.sschmid.hcc.sbv1.utility.CSV;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LanguageDetectionTest {
  
  private static final Logger LOGGER;
  private static final boolean CREATE_CSV = false;
  private static final String FILES_DIR = "D:\\Documents\\Dropbox\\FH HGB\\HCC\\Semester "
      + "1\\SBV1\\UE\\UE02\\files\\lang\\modified\\";
  private static final String[] LANGUAGES = new String[] { "de", "en", "nl", "es", "prt", "fr", "it", "pl" };
  
  static {
    LOGGER = Logger.getLogger(LanguageDetectionTest.class.getName());
    LOGGER.setLevel(Level.INFO);
  }
  
  private final Map<String, String> refTexts = new HashMap<>(LANGUAGES.length);
  private final Map<String, Double> refCrs = new HashMap<>(LANGUAGES.length);
  private final Map<String, String> signalTexts = new HashMap<>(LANGUAGES.length);
  
  @Before
  public void setup() throws IOException {
    for (final String language : LANGUAGES) {
      final String referenceText = readFile(String.format("%srefs\\text_%s.txt", FILES_DIR, language));
      refTexts.put(language, referenceText);
      
      final LempelZivWelch zipOriginal = LempelZivWelch.encode(refTexts.get(language));
      refCrs.put(language, zipOriginal.getCompressionRatio());
      
      signalTexts.put(language, readFile(String.format("%ssignals\\detect_%s.txt", FILES_DIR, language)));
    }
  }
  
  /**
   * Zipped on Ubuntu using `find . -type f -execdir zip '{}.zip' '{}' \;`
   */
  @Test
  @Ignore
  public void generateCombinedTextFiles() throws IOException {
    for (final String signalLang : LANGUAGES) {
      for (final String refLang : LANGUAGES) {
        final String referenceText = readFile(String.format("%srefs\\text_%s.txt", FILES_DIR, refLang));
        final String signalText = readFile(String.format("%ssignals\\detect_%s.txt", FILES_DIR, signalLang));
        final String combinedText = new StringBuilder(referenceText).append(signalText).toString();
        final File file = new File(String.format("%scombined\\combined_%s_%s.txt", FILES_DIR, refLang, signalLang));
        if (!file.createNewFile()) {
          throw new IOException("Could not create file " + file.getName());
        }
        
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
          writer.write(combinedText);
        }
      }
    }
  }
  
  @Test
  @Ignore
  public void fileAnalysis() {
    CSV.setDefaultConfig(CSV.Config.defaults().withOutputDirectory(FILES_DIR + "\\csv\\"));
    try (final CSV csv = new CSV("analysis")) {
      csv.open();
      
      final FilebasedCRDelta[][] confusionMatrix = new FilebasedCRDelta[LANGUAGES.length][LANGUAGES.length];
      
      int x = 0;
      for (final String signalLang : LANGUAGES) {
        final List<FilebasedCRDelta> ratios = new ArrayList<>(LANGUAGES.length);
        int y = 0;
        for (final String refLang : LANGUAGES) {
          final File refFile = getFile(String.format("%s\\refs\\text_%s.txt", FILES_DIR, refLang));
          final File refZipFile = getFile(String.format("%s\\refs-zip\\text_%s.txt.zip", FILES_DIR, refLang));
          final File combinedFile = getFile(String.format("%s\\combined\\combined_%s_%s.txt",
              FILES_DIR,
              refLang,
              signalLang));
          final File combinedZipFile = getFile(String.format("%s\\combined-zip\\combined_%s_%s.txt.zip",
              FILES_DIR,
              refLang,
              signalLang));
          
          final FilebasedCRDelta delta = new FilebasedCRDelta(refLang,
              refFile.length(),
              refZipFile.length(),
              combinedFile.length(),
              combinedZipFile.length());
          ratios.add(delta);
          confusionMatrix[x][y++] = delta;
        }
        
        ratios.sort(FilebasedCRDelta::compareTo);
        
        csv.addRow(csv.row().cell("Signal:").cell(signalLang.toUpperCase()))
            .addRow(csv.row()
                .cell("Ref. Lang")
                .cell("Ref. Bytes")
                .cell("Ref. Bytes (zipped)")
                .cell("CR (reference)")
                .cell("Comb. Bytes")
                .cell("Comb. Bytes (zipped)")
                .cell("CR (combined)")
                .cell("CR (delta)"));
        
        for (final FilebasedCRDelta ratio : ratios) {
          csv.addRow(csv.row()
              .cell(ratio.refLang.toUpperCase())
              .cell(ratio.bytesRef)
              .cell(ratio.bytesRefZip)
              .cell(String.format("%.4f", ratio.bytesRatio_ref_refZip))
              .cell(ratio.bytesCombined)
              .cell(ratio.bytesCombinedZip)
              .cell(String.format("%.4f", ratio.bytesRatio_combined_CombinedZip))
              .cell(String.format("%.4f", ratio.bytesRatioDelta)));
        }
        
        csv.emptyRow();
        x++;
      }
      
      csv.emptyRow().addRow(csv.row().cell("CONFUSION MATRIX"));
      final CSV.Row headerRow = csv.row().empty().cell("B(r)").cell("CR(r)");
      final CSV.Row subHeaderRow = csv.row().empty(3);
      for (final String lang : LANGUAGES) {
        headerRow.cell(lang.toUpperCase()).empty(3);
        subHeaderRow.cell("Rank").cell("B(r+s)").cell("CR(r+s)").cell("CR(d)");
      }
      csv.addRow(headerRow).addRow(subHeaderRow);
      
      for (x = 0; x < confusionMatrix.length; x++) {
        final String lang = LANGUAGES[x];
        final List<FilebasedCRDelta> sortedDeltas = Arrays.stream(confusionMatrix[x])
            .sorted()
            .collect(Collectors.toList());
        final FilebasedCRDelta langDelta = sortedDeltas.stream().filter(d -> d.refLang.equals(lang)).findFirst().get();
        final CSV.Row row = csv.row()
            .cell(lang.toUpperCase())
            .cell(langDelta.bytesRef)
            .cell(langDelta.bytesRatio_ref_refZip);
        final CSV.Row row2 = csv.row().empty(3);
        for (int y = 0; y < confusionMatrix[x].length; y++) {
          final FilebasedCRDelta delta = confusionMatrix[x][y];
          row.cell(sortedDeltas.indexOf(delta) + 1)
              .cell(delta.bytesCombined)
              .cell(delta.bytesRatio_combined_CombinedZip)
              .cell(delta.bytesRatioDelta);
          row2.empty(3).cell(delta.bytesRatioDeltaRelative);
        }
        csv.addRow(row).addRow(row2);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  @Test
  public void detectGerman() {
    detectSignalLanguage("de");
  }
  
  @Test
  public void detectEnglish() {
    detectSignalLanguage("en");
  }
  
  @Test
  public void detectDutch() {
    detectSignalLanguage("nl");
  }
  
  @Test
  public void detectFrench() {
    detectSignalLanguage("fr");
  }
  
  @Test
  public void detectSpanish() {
    detectSignalLanguage("es");
  }
  
  @Test
  public void detectPortuguese() {
    detectSignalLanguage("prt");
  }
  
  @Test
  public void detectItalian() {
    detectSignalLanguage("it");
  }
  
  @Test
  public void detectPolish() {
    detectSignalLanguage("pl");
  }
  
  private File getFile(final String fileName) throws IOException {
    final File refFile = new File(fileName);
    if (!refFile.canRead()) {
      throw new IOException("Cannot read '" + fileName + "'");
    }
    return refFile;
  }
  
  private String readFile(final String fileName) throws IOException {
    StringBuilder result = new StringBuilder();
    final File file = getFile(fileName);
    try (final Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(file),
        StandardCharsets.UTF_8)))) {
      while (scanner.hasNextLine()) {
        result.append(scanner.nextLine().toLowerCase());
      }
    }
    
    return result.toString();
  }
  
  private void detectSignalLanguage(final String signalLanguage) {
    CSV.setDefaultConfig(CSV.Config.defaults().withOutputDirectory(FILES_DIR + "\\csv\\"));
    try (final CSV csv = new CSV(signalLanguage)) {
      if (CREATE_CSV) {
        csv.open();
      }
      
      final String signalText = signalTexts.get(signalLanguage);
      if (CREATE_CSV) {
        final LempelZivWelch signalZip = LempelZivWelch.encode(signalText);
        csv.addRow(csv.row().cell(String.format("SIGNAL (%s):", signalLanguage)))
            .addRow(csv.row().cell("CR:").cell(signalZip.getCompressionRatio()))
            .emptyRow()
            .addRow(csv.row().cell("DELTAS:"));
      }
      
      final SortedSet<CRDelta> deltas = new TreeSet<>();
      for (final Map.Entry<String, Double> cr : refCrs.entrySet()) {
        final String refLang = cr.getKey();
        final String combinedText = new StringBuilder(refTexts.get(refLang)).append(signalText).toString();
        final LempelZivWelch refZip = LempelZivWelch.encode(refTexts.get(refLang));
        final LempelZivWelch combinedZip = LempelZivWelch.encode(combinedText);
        deltas.add(new CRDelta(refLang, refZip.getCompressionRatio(), combinedZip.getCompressionRatio()));
      }
      
      if (CREATE_CSV) {
        for (final CRDelta delta : deltas) {
          csv.addRow(csv.row().cell(String.format("CR (%s)", delta.refLang)).cell(delta.crRef))
              .addRow(csv.row()
                  .cell(String.format("CR (combined %s-%s):", delta.refLang, signalLanguage))
                  .cell(delta.crCombined))
              .addRow(csv.row().cell("CR (delta):").cell(delta.crDelta))
              .emptyRow();
        }
      }
      
      Assert.assertEquals(signalLanguage, deltas.first().refLang);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private static class CRDelta implements Comparable<CRDelta> {
    
    private final String refLang;
    private final double crRef;
    private final double crCombined;
    private final double crDelta;
    
    private CRDelta(final String refLang, final double crRef, final double crCombined) {
      this.refLang = refLang;
      this.crRef = crRef;
      this.crCombined = crCombined;
      this.crDelta = crRef - crCombined;
    }
    
    @Override
    public int compareTo(final CRDelta other) {
      return Double.compare(crDelta, other.crDelta);
    }
    
  }
  
  private static class FilebasedCRDelta implements Comparable<FilebasedCRDelta> {
    
    private final String refLang;
    private final long bytesRef;
    private final long bytesRefZip;
    private final long bytesCombined;
    private final long bytesCombinedZip;
    private final double bytesRatio_ref_refZip;
    private final double bytesRatio_combined_CombinedZip;
    private final double bytesRatioDelta;
    private final double bytesRatioDeltaRelative;
    
    private FilebasedCRDelta(final String refLang,
                             final long bytesRef,
                             final long bytesRefZip,
                             final long bytesCombined,
                             final long bytesCombinedZip) {
      this.refLang = refLang;
      this.bytesRef = bytesRef;
      this.bytesRefZip = bytesRefZip;
      this.bytesCombined = bytesCombined;
      this.bytesCombinedZip = bytesCombinedZip;
      this.bytesRatio_ref_refZip = bytesRef / (double) bytesRefZip;
      this.bytesRatio_combined_CombinedZip = bytesCombined / (double) bytesCombinedZip;
      this.bytesRatioDelta = bytesRatio_ref_refZip - bytesRatio_combined_CombinedZip;
      this.bytesRatioDeltaRelative = bytesRatioDelta / bytesRatio_ref_refZip;
    }
    
    @Override
    public int compareTo(final FilebasedCRDelta other) {
      return Double.compare(bytesRatioDelta, other.bytesRatioDelta);
    }
    
  }
  
}
