package at.sschmid.hcc.sbv1.compression;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LempelZivWelchTest {
  
  private static final Logger LOGGER;
  private static final boolean CSV = false;
  
  static {
    LOGGER = Logger.getLogger(LempelZivWelchTest.class.getName());
    LOGGER.setLevel(Level.INFO);
  }
  
  @Test
  public void encoding1() {
    final String input = "ababbbaab";
    final LempelZivWelch zip = LempelZivWelch.encode(input);
  
    csv(zip, "encoding1");
    
    final int resultSize = zip.getResultSize();
    final double compressionRatio = zip.getCompressionRatio();
  
    Assert.assertEquals(resultSize, zip.getSteps().size());
    Assert.assertEquals("97|98|256|98|257|256", zip.getResult());
    Assert.assertEquals(6, resultSize);
    Assert.assertEquals(input, zip.getDecodedResult());
    Assert.assertEquals(1.5, compressionRatio, 0);
    Assert.assertEquals(input.length(), (int) (resultSize * compressionRatio));
    Assert.assertEquals("ab <256>, ba <257>, abb <258>, bb <259>, baa <260>", zip.getDictionary());
  }
  
  @Test
  public void encoding2() {
    final String input = "ababbbaabbcdcdcdcabbc";
    final LempelZivWelch zip = LempelZivWelch.encode(input);
  
    csv(zip, "encoding2");
    
    final int resultSize = zip.getResultSize();
    final double compressionRatio = zip.getCompressionRatio();
  
    Assert.assertEquals(resultSize, zip.getSteps().size());
    Assert.assertEquals("97|98|256|98|257|258|99|100|262|264|261", zip.getResult());
    Assert.assertEquals(11, resultSize);
    Assert.assertEquals(input, zip.getDecodedResult());
    Assert.assertEquals(1.909, compressionRatio, 0.001);
    Assert.assertEquals(input.length(), (int) (resultSize * compressionRatio));
    Assert.assertEquals(
        "ab <256>, ba <257>, abb <258>, bb <259>, baa <260>, abbc <261>, cd <262>, dc <263>, cdc <264>"
            + ", cdca <265>",
        zip.getDictionary());
  }
  
  @Test
  public void encoding3() {
    final String input = "abababbbaaaabababccddda";
    final LempelZivWelch zip = LempelZivWelch.encode(input);
  
    csv(zip, "encoding3");
    
    final int resultSize = zip.getResultSize();
    final double compressionRatio = zip.getCompressionRatio();
  
    Assert.assertEquals(resultSize, zip.getSteps().size());
    Assert.assertEquals("97|98|256|256|98|257|97|262|257|264|99|99|100|268|97", zip.getResult());
    Assert.assertEquals(15, resultSize);
    Assert.assertEquals(input, zip.getDecodedResult());
    Assert.assertEquals(1.533, compressionRatio, 0.001);
    Assert.assertEquals(input.length(), (int) (resultSize * compressionRatio));
    Assert.assertEquals(
        "ab <256>, ba <257>, aba <258>, abb <259>, bb <260>, baa <261>, aa <262>, aab <263>, bab <264>"
            + ", babc <265>, cc <266>, cd <267>, dd <268>, dda <269>",
        zip.getDictionary());
  }
  
  @Test
  public void encoding4() {
    final StringBuilder inputBuilder = new StringBuilder();
    final int iz = (int) 'z';
    for (int i = (int) 'a'; i <= iz; i++) {
      inputBuilder.append((char) i);
    }
    
    final String input = inputBuilder.toString();
    final LempelZivWelch zip = LempelZivWelch.encode(input);
    
    csv(zip, "encoding-abc");
    
    final int resultSize = zip.getResultSize();
    final double compressionRatio = zip.getCompressionRatio();
    
    Assert.assertEquals(resultSize, zip.getSteps().size());
    Assert.assertEquals(26, resultSize);
    Assert.assertEquals(input, zip.getDecodedResult());
    Assert.assertEquals(1, compressionRatio, 0);
    Assert.assertEquals(input.length(), (int) (resultSize * compressionRatio));
  }
  
  @Test
  public void encoding5() {
    final StringBuilder inputBuilder = new StringBuilder();
    final int length = 26; // cr15=3 (best usage of dict) | cr16=2.6 | cr26=3.7 | cr28=4 (best usage of dict)
    for (int i = 0; i < length; i++) {
      inputBuilder.append('a');
    }
    
    final String input = inputBuilder.toString();
    final LempelZivWelch zip = LempelZivWelch.encode(input);
    
    csv(zip, String.format("encoding-a%d", length));
    
    final int resultSize = zip.getResultSize();
    final double compressionRatio = zip.getCompressionRatio();
    
    Assert.assertEquals(resultSize, zip.getSteps().size());
    Assert.assertEquals(7, resultSize);
    Assert.assertEquals(input, zip.getDecodedResult());
    Assert.assertEquals(3.714, compressionRatio, 0.001);
    Assert.assertEquals(input.length(), (int) (resultSize * compressionRatio));
  }
  
  private void csv(final LempelZivWelch zip, final String fileNameSuffix) {
    if (!CSV) {
      return;
    }
    
    try {
      zip.csv(fileNameSuffix);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
}
