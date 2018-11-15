package at.sschmid.hcc.sbv1.compression;

import org.junit.Assert;
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
    final String input = "ababba";
    final HuffmanCoding coding = HuffmanCoding.encode(input);
  
    final byte bitsPerSymbol = coding.getBitsPerSymbol();
    final int resultSize = coding.getResultSize();
    final double compressionRatio = coding.getCompressionRatio();
  
    Assert.assertEquals("101001", coding.getResult());
    Assert.assertEquals("1|0|1|0|0|1", coding.getDelimitedResult());
    Assert.assertEquals(6, resultSize);
    Assert.assertEquals(input, coding.getDecodedResult());
    Assert.assertEquals(1, bitsPerSymbol);
    Assert.assertEquals(1, compressionRatio, 0);
    Assert.assertEquals(input.length() * bitsPerSymbol, (int) (resultSize * compressionRatio));
  }
  
  @Test
  public void encoding2() {
    final String input = "abababbbaaaabababccddda";
    final HuffmanCoding coding = HuffmanCoding.encode(input);
    
    final byte bitsPerSymbol = coding.getBitsPerSymbol();
    final int resultSize = coding.getResultSize();
    final double compressionRatio = coding.getCompressionRatio();
    
    Assert.assertEquals("10110110101011111011011010000000010010011", coding.getResult());
    Assert.assertEquals("1|01|1|01|1|01|01|01|1|1|1|1|01|1|01|1|01|000|000|001|001|001|1", coding.getDelimitedResult());
    Assert.assertEquals(41, resultSize);
    Assert.assertEquals(input, coding.getDecodedResult());
    Assert.assertEquals(2, bitsPerSymbol);
    Assert.assertEquals(1.12195, compressionRatio, 0.001);
    Assert.assertEquals(input.length() * bitsPerSymbol, (int) (resultSize * compressionRatio));
  }
  
  @Test
  public void encoding3() {
    final String input = "abababbbaaeeabababccddda";
    final HuffmanCoding coding = HuffmanCoding.encode(input);
    
    final byte bitsPerSymbol = coding.getBitsPerSymbol();
    final int resultSize = coding.getResultSize();
    final double compressionRatio = coding.getCompressionRatio();
    
    Assert.assertEquals("10110110101011100000000101101101000100010010010011", coding.getResult());
    Assert.assertEquals("1|01|1|01|1|01|01|01|1|1|0000|0000|1|01|1|01|1|01|0001|0001|001|001|001|1",
        coding.getDelimitedResult());
    Assert.assertEquals(50, resultSize);
    Assert.assertEquals(input, coding.getDecodedResult());
    Assert.assertEquals(3, bitsPerSymbol);
    Assert.assertEquals(1.44, compressionRatio, 0.001);
    Assert.assertEquals(input.length() * bitsPerSymbol, (int) (resultSize * compressionRatio));
  }
  
  @Test
  public void encodingMinCr() {
//    final String input = "abcdefghij";
    final String input = "aaabbbcdefghij";
    final HuffmanCoding coding = HuffmanCoding.encode(input);
    
    LOGGER.info(String.format("Huffman('%s') = %s, CR=%.4f",
        input,
        coding.getDelimitedResult(),
        coding.getCompressionRatio()));
  }
  
  @Test
  public void encodingMaxCr() {
//    final String input = "aaaaaaaaab";
//    final String input = "aaaaabaaca";
    final String input = "aaaaaaaabc";
//    final String input = "aaaaaaaaaa";
    final HuffmanCoding coding = HuffmanCoding.encode(input);
    
    LOGGER.info(String.format("Huffman('%s') = %s, CR=%.4f",
        input,
        coding.getDelimitedResult(),
        coding.getCompressionRatio()));
  }
  
}
