package at.sschmid.hcc.sbv1.compression;

import at.sschmid.hcc.sbv1.utility.CSV;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class LempelZivWelch {
  
  public static LempelZivWelch encode(final String input) {
    final LempelZivWelch zip = new LempelZivWelch(input);
    zip.encode();
    return zip;
  }
  
  private static final String FILES_SUB_DIR = "UE02\\files\\lempel-ziv-welch";
  private static final int DICT_START = 256;
  
  private final String input;
  private final List<Step> steps = new LinkedList<>();
  private final Map<String, Integer> dictionary = new HashMap<>();
  private final List<Integer> result = new LinkedList<>();
  
  private int maxTranslation = DICT_START;
  private Map<Integer, String> invertedDictionary;
  
  private LempelZivWelch(final String input) {
    this.input = input;
  }
  
  public String getInput() {
    return input;
  }
  
  public List<Step> getSteps() {
    return steps;
  }
  
  public String getResult() {
    return result.stream().map(v -> Integer.toString(v)).collect(Collectors.joining("|"));
  }
  
  public int getResultSize() {
    return result.size();
  }
  
  public double getCompressionRatio() {
    return input.length() / (double) this.getResultSize();
  }
  
  public String getDecodedResult() {
    buildInvertedDictionary();
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
  
  public void csv(final String fileNameSuffix) throws IOException {
    final String fileName = String.format("%s_%s", getClass().getSimpleName(), fileNameSuffix);
    try (final CSV csv = new CSV(fileName, FILES_SUB_DIR)) {
      csv.open();
      
      final int inputLength = input.length();
      final int resultLength = getResultSize();
      csv.addRow(csv.row().cell(String.format("INPUT (%d):", inputLength)).cell(input))
          .addRow(csv.row().cell(String.format("RESULT (%d):", resultLength)).cell(getResult()))
          .addRow(csv.row()
              .cell("CR:")
              .cell(String.format("1 / (%d / %d) = %.3f", resultLength, inputLength, getCompressionRatio())));
      
      if (!steps.isEmpty()) {
        csv.emptyRow()
            .addRow(csv.row().cell(String.format("%d STEPS:", steps.size())))
            .addRow(csv.row()
                .cell("Symbol Index")
                .cell("Current Symbol")
                .cell("Next Symbol")
                .cell("Words")
                .cell("Dictionary")
                .cell("Result"));
        
        for (final Step step : steps) {
          csv.addRow(csv.row()
              .cell(step.symbolIndex)
              .cell(step.currentSymbol)
              .cell(step.nextSymbol == null ? "<none>" : String.valueOf(step.nextSymbol))
              .cell(step.words.isEmpty() ? "<empty>" : String.join(", ", step.words))
              .cell(step.dictionaryWord == null
                  ? "<none>"
                  : String.format("%s <%d>", step.dictionaryWord, dictionary.get(step.dictionaryWord)))
              .cell(String.format("%s <%d>", step.output.getKey(), step.output.getValue())));
        }
      }
      
      csv.emptyRow();
      if (dictionary.isEmpty()) {
        csv.addRow(csv.row().cell("Dictionary is empty."));
      } else {
        csv.addRow(csv.row().cell(String.format("%d DICTIONARY ENTRIES:", dictionary.size())))
            .addRow(csv.row().cell("Value").cell("Translation"));
        for (final Map.Entry<String, Integer> entry : dictionary.entrySet()
            .stream()
            .sorted(Comparator.comparingInt(Map.Entry::getValue))
            .collect(Collectors.toList())) {
          csv.addRow(csv.row().cell(entry.getValue()).cell(entry.getKey()));
        }
      }
      
      buildInvertedDictionary();
      csv.emptyRow()
          .addRow(csv.row().cell("RESULT:"))
          .addRow(csv.row().cell("Output").cell("Translation"));
      for (final int value : result) {
        final CSV.Row row = csv.row().cell(value);
        if (value < DICT_START) {
          row.cell((char) value);
        } else {
          row.cell(invertedDictionary.get(value));
        }
        csv.addRow(row);
      }
    }
  }
  
  private void encode() {
    if (result.size() > 0 || dictionary.size() > 0) {
      return; // do not encode twice
    }
    
    final int inputLength = input.length();
    for (int i = 0; i < inputLength; i++) {
      int nextSymbolIndex = i;
      final char currentSymbol = input.charAt(nextSymbolIndex++);
      int output = (int) currentSymbol;
      
      if (nextSymbolIndex == inputLength) {
        // already at last character in input string
        steps.add(new StepBuilder(i, currentSymbol).build());
        result.add(output);
        continue;
      }
      
      final char nextSymbol = input.charAt(nextSymbolIndex++);
      final StepBuilder step = new StepBuilder(i, currentSymbol, nextSymbol);
      
      String word = null;
      int translation = -1;
      String nextWord = String.format("%s%s", currentSymbol, nextSymbol);
      step.addWord(nextWord);
      Integer nextTranslation = dictionary.get(nextWord);
      while (nextTranslation != null && i < inputLength) {
        word = nextWord;
        step.addWord(nextWord);
        translation = nextTranslation;
        if (nextSymbolIndex < inputLength) {
          nextWord += String.valueOf(input.charAt(nextSymbolIndex++));
          nextTranslation = dictionary.get(nextWord);
        }
        i++; // skip next characters included in used translation
      }
      
      if (!dictionary.containsKey(nextWord)) {
        step.addWord(nextWord);
        step.setDictionaryWord(nextWord);
        dictionary.put(nextWord, maxTranslation++);
      }
      
      if (word != null) {
        step.setOutput(word, translation);
        output = translation;
      } else {
        step.setOutput(String.valueOf(currentSymbol), output);
      }
      
      steps.add(step.build());
      result.add(output);
    }
  }
  
  private void buildInvertedDictionary() {
    if (invertedDictionary == null) {
      invertedDictionary = dictionary.entrySet()
          .stream()
          .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }
  }
  
  public class Step {
    
    private final int symbolIndex;
    private final char currentSymbol;
    private final Character nextSymbol;
    private final SortedSet<String> words;
    private final String dictionaryWord;
    private final Map.Entry<String, Integer> output;
    
    private Step(final int symbolIndex,
                 final char currentSymbol,
                 final Character nextSymbol,
                 final SortedSet<String> words,
                 final String dictionaryWord,
                 final Map.Entry<String, Integer> output) {
      this.symbolIndex = symbolIndex;
      this.currentSymbol = currentSymbol;
      this.nextSymbol = nextSymbol;
      this.words = words;
      this.dictionaryWord = dictionaryWord;
      this.output = output;
    }
    
  }
  
  private class StepBuilder {
    
    private final int symbolIndex;
    private final char currentSymbol;
    private final Character nextSymbol;
    private final SortedSet<String> words = new TreeSet<>(Comparator.comparingInt(String::length));
    
    private String dictionaryWord;
    private Map.Entry<String, Integer> output;
    
    private StepBuilder(final int symbolIndex, final char currentSymbol) {
      this(symbolIndex, currentSymbol, null);
      setOutput(String.valueOf(currentSymbol), (int) currentSymbol);
    }
    
    private StepBuilder(final int symbolIndex, final char currentSymbol, final Character nextSymbol) {
      this.symbolIndex = symbolIndex;
      this.currentSymbol = currentSymbol;
      this.nextSymbol = nextSymbol;
    }
    
    private void addWord(final String word) {
      words.add(word);
    }
    
    private void setDictionaryWord(final String dictionaryWord) {
      this.dictionaryWord = dictionaryWord;
    }
    
    private void setOutput(final String word, final int output) {
      this.output = new AbstractMap.SimpleImmutableEntry<>(word, output);
    }
    
    private Step build() {
      return new Step(symbolIndex, currentSymbol, nextSymbol, words, dictionaryWord, output);
    }
    
  }
  
}
