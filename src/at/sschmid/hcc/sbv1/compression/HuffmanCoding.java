package at.sschmid.hcc.sbv1.compression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class HuffmanCoding {
  
  public static HuffmanCoding encode(final String input) {
    final HuffmanCoding huffmanCoding = new HuffmanCoding(input);
    huffmanCoding.encode();
    return huffmanCoding;
  }
  
  private final String input;
  
  private Map<Character, HuffmanSymbol> histogram;
  private String result;
  
  private HuffmanCoding(final String input) {
    this.input = input;
  }
  
  public String getResult() {
    return result;
  }
  
  public String getDelimitedResult() {
    buildHistogram();
    
    final StringBuilder delimitedResult = new StringBuilder();
    final int resultLength = result.length();
    final int maxWordLength = histogram.size() - 1;
    for (int i = 0; i < resultLength; ) {
      final int wordEnd = i + maxWordLength;
      boolean wordCompleted = false;
      do {
        final char nextChar = result.charAt(i);
        if (nextChar == '1') {
          wordCompleted = true;
        }
        delimitedResult.append(nextChar);
        i++;
      } while (i < resultLength && i < wordEnd && !wordCompleted);
      delimitedResult.append('|');
    }
    
    final int lastCharIndex = delimitedResult.length() - 1;
    if (lastCharIndex >= 0 && delimitedResult.charAt(lastCharIndex) == '|') {
      delimitedResult.deleteCharAt(lastCharIndex);
    }
    
    return delimitedResult.toString();
  }
  
  public String getDecodedResult() {
    final Map<String, Character> dictionary = histogram.values()
        .stream()
        .collect(Collectors.toMap(v -> v.code, v -> v.symbol));
    
    final StringBuilder decodedResult = new StringBuilder();
    final String[] words = getDelimitedResult().split(Pattern.quote("|"));
    for (final String word : words) {
      decodedResult.append(dictionary.get(word));
    }
    
    return decodedResult.toString();
  }
  
  public int getResultSize() {
    return result.length();
  }
  
  public double getCompressionRatio() {
    final double totalBytes = input.length() * getBitsPerSymbol();
    final int resultSize = getResultSize();
    return resultSize > 0 ? totalBytes / resultSize : 0;
  }
  
  public List<HuffmanSymbol> getSymbols() {
    return histogram.values().stream().sorted().collect(Collectors.toList());
  }
  
  public byte getBitsPerSymbol() {
    buildHistogram();
    final int symbolCount = histogram.size();
    byte bits = 1;
    if (symbolCount > 2) {
      int bitsSquare;
      do {
        bits++;
        bitsSquare = bits * bits;
      } while (bitsSquare < symbolCount);
    }
    
    return bits;
  }
  
  private void encode() {
    if (result != null) {
      return; // do not encode twice
    }
  
    buildHistogram();
  
    // Sort huffman symbols and assign each symbol's code
    final List<HuffmanSymbol> symbols = getSymbols();
    final int symbolsLength = symbols.size();
    final StringBuilder codeBuilder = new StringBuilder("1");
    int i = 0;
    if (symbolsLength == 1) {
      symbols.get(i).setCode(codeBuilder.toString());
    } else {
      for (; i < symbolsLength - 1; i++) {
        final HuffmanSymbol symbol = symbols.get(i);
        symbol.setCode(codeBuilder.toString());
        codeBuilder.insert(0, "0");
      }
    
      if (i < symbolsLength) {
        symbols.get(i).setCode(codeBuilder.substring(0, codeBuilder.length() - 1));
      }
    }
  
    // Build final result
    final StringBuilder resultBuilder = new StringBuilder();
    final int inputLength = input.length();
    for (i = 0; i < inputLength; i++) {
      final HuffmanSymbol symbol = histogram.get(input.charAt(i));
      resultBuilder.append(symbol.code);
    }
  
    result = resultBuilder.toString();
  }
  
  private void buildHistogram() {
    if (histogram != null) {
      return;
    }
    
    // Build histogram and calculate probabilities
    final int inputLength = input.length();
    histogram = new HashMap<>();
    for (int i = 0; i < inputLength; i++) {
      final char currentSymbol = input.charAt(i);
      HuffmanSymbol symbol = histogram.get(currentSymbol);
      if (symbol == null) {
        symbol = new HuffmanSymbol(currentSymbol);
        histogram.put(currentSymbol, symbol);
      } else {
        symbol.increaseOccurrences();
      }
    }
  }
  
  public class HuffmanSymbol implements Comparable<HuffmanSymbol> {
    
    private final char symbol;
    
    private int occurrences = 1;
    private double probability;
    private String code;
    
    private HuffmanSymbol(final char symbol) {
      this.symbol = symbol;
      updateProbability();
    }
    
    @Override
    public int compareTo(final HuffmanSymbol other) {
      return Double.compare(other.probability, probability);
    }
    
    @Override
    public String toString() {
      return String.format("HuffmanSymbol '%s' { occurrences=%d, probability=%.3f, code='%s' }",
          symbol,
          occurrences,
          probability,
          code);
    }
    
    private void setCode(final String code) {
      this.code = code;
    }
    
    private void increaseOccurrences() {
      occurrences++;
      updateProbability();
    }
    
    private void updateProbability() {
      probability = occurrences / (double) input.length();
    }
    
  }
  
}
