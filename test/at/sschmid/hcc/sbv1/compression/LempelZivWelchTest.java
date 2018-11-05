package at.sschmid.hcc.sbv1.compression;

import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LempelZivWelchTest {
  
  private static final Logger LOGGER;
  
  static {
    LOGGER = Logger.getLogger(LempelZivWelchTest.class.getName());
    LOGGER.setLevel(Level.INFO);
  }
  
  @Test
  public void encoding1() {
    final String input = "ababbbaab";
    final LempelZivWelch zip = LempelZivWelch.encode(input);
    
    final int resultSize = zip.getResultSize();
    final double compressionRatio = zip.getCompressionRatio();
    
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
    
    final int resultSize = zip.getResultSize();
    final double compressionRatio = zip.getCompressionRatio();
    
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
    
    final int resultSize = zip.getResultSize();
    final double compressionRatio = zip.getCompressionRatio();
    
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
  
}
