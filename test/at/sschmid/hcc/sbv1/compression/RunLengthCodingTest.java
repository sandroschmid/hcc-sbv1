package at.sschmid.hcc.sbv1.compression;

import org.junit.Assert;
import org.junit.Test;

public class RunLengthCodingTest {
  
  private static final RunLengthCoding.BinaryValue BINARY_START = RunLengthCoding.BinaryValue.Zero;
  private static final int MAX_OCCURRENCES_TEST = 3;
  
  @Test
  public void binaryZeroStart() {
    final RunLengthCoding.Config config = new RunLengthCoding.Config()
        .withBinary(true)
        .withBinaryStart(BINARY_START);
    
    test("010101", "111111", 1, config);
    test("0101111001", "111421", 1.6667, config);
    
    config.setMaxOccurrences(MAX_OCCURRENCES_TEST);
    test("010101", "111111", 1, config);
    test("0101111001", "11130121", 1.25, config);
  }
  
  @Test
  public void binaryOneStart() {
    final RunLengthCoding.Config config = new RunLengthCoding.Config()
        .withBinary(true)
        .withBinaryStart(BINARY_START);
    
    test("101010", "0111111", 0.8571, config);
    test("101011111001", "01111521", 1.5, config);
    
    config.setMaxOccurrences(MAX_OCCURRENCES_TEST);
    test("101010", "0111111", 0.8571, config);
    test("101011111001", "0111130221", 1.2, config);
  }
  
  @Test
  public void nonBinary() {
    final RunLengthCoding.Config config = new RunLengthCoding.Config();
    
    test("aaaaaaaaa", "a9", 4.5, config);
    test("aaaaaaaab", "a8b", 3, config);
    test("aaaabaaaa", "a4ba4", 1.8, config);
    test("abcdefghij", "abcdefghij", 1, config);
    test("ababababab", "ababababab", 1, config);
    
    test("ab", "ab", 1, config);
    test("aabb", "a2b2", 1, config);
    test("abcaaaacbbaaccc", "abca4cb2a2c3", 1.25, config);
    test("abbcaaaaaaaaacbbaaccc", "ab2ca9cb2a2c3", 1.6154, config);
    test("abbcaaaaacbbaacccd", "ab2ca5cb2a2c3d", 1.2857, config);
    
    config.setMaxOccurrences(MAX_OCCURRENCES_TEST);
    test("ab", "ab", 1, config);
    test("aabb", "a2b2", 1, config);
    test("abcaaaacbbaaccc", "abca3acb2a2c3", 1.1538, config);
    test("abbcaaaaaaaaacbbaaccc", "ab2ca3a3a3cb2a2c3", 1.2353, config);
    test("abbcaaaaacbbaacccd", "ab2ca3a2cb2a2c3d", 1.125, config);
  }
  
  private void test(final String input,
                    final String encodedResult,
                    final double cr,
                    final RunLengthCoding.Config config) {
    final RunLengthCoding encoded = RunLengthCoding.encode(input, config);
    final RunLengthCoding decoded = RunLengthCoding.decode(encoded.getResult(), config);
    
    Assert.assertEquals(encodedResult, encoded.getResult());
    Assert.assertEquals(input, decoded.getResult());
    Assert.assertEquals(cr, encoded.getCompressionRatio(), 0.0001);
  }
  
}
