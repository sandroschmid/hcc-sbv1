package at.sschmid.hcc.sbv1.compression;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class EntropieTest {
  
  @Test
  public void test1() {
    test("aaaaaaaaaa");
    test("ababababab");
    test("abcdefghij");
    test("111122661112233334564511211111");
  }
  
  private void test(final String input) {
    final Map<Character, Double> probabilities = getProbabilities(input);
    System.out.println(String.format("entropie('%s') = %.3f", input, entropie(probabilities)));
  }
  
  private Map<Character, Double> getProbabilities(final String input) {
    final Map<Character, Double> probabilities = new HashMap<>();
    final char[] characters = new char[input.length()];
    input.getChars(0, input.length(), characters, 0);
    
    // count all characters
    for (final char character : characters) {
      if (probabilities.containsKey(character)) {
        probabilities.put(character, probabilities.get(character) + 1);
      } else {
        probabilities.put(character, 1d);
      }
    }
    
    // calc probabilities
    for (final Map.Entry<Character, Double> character : probabilities.entrySet()) {
      probabilities.put(character.getKey(), character.getValue() / characters.length);
    }
    
    return probabilities;
  }
  
  private double entropie(final Map<Character, Double> probabilities) {
//    final double y = binLog(probabilities.size());
    double sum = 0d;
    for (double p : probabilities.values()) {
//      sum += p * y;
      sum += p * binLog(p);
    }
    return -sum;
  }
  
  private double binLog(double x) {
    return Math.log(x) / Math.log(2);
  }
  
}
