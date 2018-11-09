package at.sschmid.hcc.sbv1.compression;

import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HuffmanCodingTest {
  
  private static final Logger LOGGER;
  
  static {
    LOGGER = Logger.getLogger(HuffmanCodingTest.class.getName());
    LOGGER.setLevel(Level.INFO);
  }
  
  @Test
  public void encoding1() {
    final String input = "abababbbaaaabababccddda";
    final HuffmanCoding coding = HuffmanCoding.encode(input);
    
//    final int resultSize = coding.getResultSize();
//    final double compressionRatio = coding.getCompressionRatio();
//
//    Assert.assertEquals("1|01|1|01|2|01|01|01|1|1|1|1|01|1|01|1|01|1|01|000|000|001|001|001|1", coding.getResult());
//    Assert.assertEquals(41, resultSize);
//    Assert.assertEquals(input, coding.getDecodedResult());
//    Assert.assertEquals(1.12195, compressionRatio, 0.001);
//    Assert.assertEquals(input.length(), (int) (resultSize * compressionRatio));
  }
  
}
