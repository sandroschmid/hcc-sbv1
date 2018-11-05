package at.sschmid.hcc.sbv1.compression;

import java.util.*;
import java.util.stream.Collectors;

public final class LempelZivWelch {
  
  public static LempelZivWelch encode(final String input) {
    final LempelZivWelch zip = new LempelZivWelch(input);
    zip.encode();
    return zip;
  }
  
  private static final int DICT_START = 256;
  
  private final String input;
  private final Map<String, Integer> dictionary = new HashMap<>();
  private final List<Integer> result = new LinkedList<>();
  
  private int maxTranslation = DICT_START;
  
  private LempelZivWelch(final String input) {
    this.input = input;
  }
  
  public String getResult() {
    return result.stream().map(v -> Integer.toString(v)).collect(Collectors.joining("|"));
  }
  
  public int getResultSize() {
    return result.size();
  }
  
  public double getCompressionRatio() {
    return 1 / (this.getResultSize() / (double) input.length());
  }
  
  public String getDecodedResult() {
    final Map<Integer, String> invertedDictionary = dictionary.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    
    return result.stream()
        .map(v -> v < DICT_START ? String.valueOf((char) (int) v) : invertedDictionary.get(v))
        .collect(Collectors.joining());
  }
  
  public String getDictionary() {
    return dictionary.entrySet()
        .stream()
        .sorted(Comparator.comparingInt(Map.Entry::getValue))
        .map(e -> String.format("%s <%d>", e.getKey(), e.getValue()))
        .collect(Collectors.joining(", "));
  }
  
  private void encode() {
    final int inputLength = input.length();
    for (int i = 0; i < inputLength; i++) {
      int nextCharIndex = i;
      final char currentChar = input.charAt(nextCharIndex++);
      Integer output = (int) currentChar;
  
      if (nextCharIndex == inputLength) {
        // already at last character in input string
        result.add(output);
        continue;
      }
  
      Integer translation = null;
      String nextWord = String.format("%s%s", currentChar, input.charAt(nextCharIndex++));
      Integer nextTranslation = dictionary.get(nextWord);
      while (nextTranslation != null && i < inputLength) {
        translation = nextTranslation;
        if (nextCharIndex < inputLength) {
          nextWord += String.valueOf(input.charAt(nextCharIndex++));
          nextTranslation = dictionary.get(nextWord);
        }
        i++; // skip upcoming characters included in used translation
      }
  
      if (!dictionary.containsKey(nextWord)) {
        dictionary.put(nextWord, maxTranslation++);
      }
  
      if (translation != null) {
        output = translation;
      }
      
      result.add(output);
    }
  }
  
}
