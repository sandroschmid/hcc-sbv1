package at.sschmid.hcc.sbv1.compression;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public final class HuffmanCoding {
  
  public static HuffmanCoding encode(final String input) {
    final HuffmanCoding huffmanCoding = new HuffmanCoding(input);
    huffmanCoding.encode();
    return huffmanCoding;
  }
  
  private static final String FILES_SUB_DIR = "UE02\\files\\huffman";
  
  private final String input;
  private final StringBuilder result = new StringBuilder();
  
  private HuffmanCoding(final String input) {
    this.input = input;
  }
  
  public String getResult() {
    final StringBuilder delimitedResult = new StringBuilder();
    final int resultLength = result.length();
    for (int i = 0; i < resultLength; i++) {
      int j = i;
      boolean wordCompleted = false;
      delimitedResult.append(result.charAt(j++));
      do {
        final char nextChar = result.charAt(j);
        if (nextChar == '1') {
          wordCompleted = true;
        }
        j++;
      } while (j < resultLength && !wordCompleted);
      delimitedResult.append('|');
    }
    
    final int lastCharIndex = delimitedResult.length() - 1;
    if (delimitedResult.charAt(lastCharIndex) == '|') {
      delimitedResult.deleteCharAt(lastCharIndex);
    }
    
    return delimitedResult.toString();
  }
  
  public int getResultSize() {
    return result.length();
  }
  
  public double getCompressionRatio() {
    return input.length() / (double) this.getResultSize();
  }
  
  public String getDecodedResult() {
    return null; // TODO
  }
  
  private void encode() {
    if (result.length() > 0) {
      return; // do not encode twice
    }
    
    final int inputLength = input.length();
    
    // build histogram for probabilities
    final Map<Character, HuffmanSymbol> histogram = new HashMap<>();
    final SortedSet<HuffmanSymbol> symbols = new TreeSet<>();
    for (int i = 0; i < inputLength; i++) {
      final char currentSymbol = input.charAt(i);
      HuffmanSymbol huffmanSymbol = histogram.get(currentSymbol);
      if (huffmanSymbol == null) {
        huffmanSymbol = new HuffmanSymbol(currentSymbol);
        histogram.put(currentSymbol, huffmanSymbol);
        symbols.add(huffmanSymbol);
      } else {
        huffmanSymbol.increaseOccurrences();
      }
    }
    
    // calculate total bytes
    final double totalBytes = histogram.size() * 2d; // TODO do not assume 2 bit / symbol are enough
    
    // set probabilities and sort huffman symbols
    for (final HuffmanSymbol symbol : symbols) {
      symbol.setProbability(symbol.occurrences / totalBytes);
    }
  
    for (final HuffmanSymbol symbol : symbols) {
      System.out.println(symbol);
    }
  }
  
  private static class HuffmanSymbol implements Comparable<HuffmanSymbol> {
    
    private final char symbol;
    
    private int occurrences;
    private double probability;
    
    private HuffmanSymbol(final char symbol) {
      this.symbol = symbol;
    }
  
    @Override
    public int compareTo(final HuffmanSymbol other) {
      return Double.compare(probability, other.probability);
    }
  
    @Override
    public String toString() {
      return String.format("HuffmanSymbol '%s' { occurrences=%d, probability=%.3f }",
          symbol,
          occurrences,
          probability);
    }
  
    private void increaseOccurrences() {
      occurrences++;
    }
  
    private void setProbability(final double probability) {
      this.probability = probability;
    }
  
  }
  
}
