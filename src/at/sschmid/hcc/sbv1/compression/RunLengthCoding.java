package at.sschmid.hcc.sbv1.compression;

public final class RunLengthCoding {
  
  private static final Config DEFAULT_CONFIG = new Config();
  
  public static RunLengthCoding encode(final String input) {
    return encode(input, DEFAULT_CONFIG);
  }
  
  public static RunLengthCoding encode(final String input, final Config config) {
    final RunLengthCoding coding = new RunLengthCoding(input, config);
    coding.encode();
    return coding;
  }
  
  public static RunLengthCoding decode(final String input) {
    return decode(input, DEFAULT_CONFIG);
  }
  
  public static RunLengthCoding decode(final String input, final Config config) {
    final RunLengthCoding coding = new RunLengthCoding(input, config);
    coding.decode();
    return coding;
  }
  
  private final String input;
  private final int inputLength;
  private final Config config;
  private final RunLengthCoder coder;
  
  private String result;
  
  private RunLengthCoding(final String input, final Config config) {
    this.input = input;
    this.inputLength = input != null ? input.length() : 0;
    this.config = config;
    this.coder = config.isBinary ? new BinaryRunLengthCoderImpl() : new RunLengthCoderImpl();
  }
  
  public String getResult() {
    return result;
  }
  
  public double getCompressionRatio() {
    return inputLength / (double) result.length();
  }
  
  private void encode() {
    if (result == null) {
      result = coder.encode();
    }
  }
  
  private void decode() {
    if (result == null) {
      result = coder.decode();
    }
  }
  
  public static class Config {
  
    private static final int MAX_MAX_OCCURRENCES = 9;
  
    private int maxOccurrences = MAX_MAX_OCCURRENCES;
    private boolean isBinary = false;
    private BinaryValue binaryStart = BinaryValue.Zero;
  
    public void setMaxOccurrences(final int maxOccurrences) {
      this.maxOccurrences = Math.max(Math.min(maxOccurrences, MAX_MAX_OCCURRENCES), 0);
    }
  
    public Config withMaxOccurrences(final int maxOccurrences) {
      setMaxOccurrences(maxOccurrences);
      return this;
    }
    
    public void setBinary(final boolean binary) {
      isBinary = binary;
    }
    
    public Config withBinary(final boolean binary) {
      setBinary(binary);
      return this;
    }
    
    public void setBinaryStart(final BinaryValue binaryStart) {
      this.binaryStart = binaryStart;
    }
    
    public Config withBinaryStart(final BinaryValue binaryStart) {
      setBinaryStart(binaryStart);
      return this;
    }
    
  }
  
  public enum BinaryValue {
    
    Zero('0'),
    One('1');
    
    private static BinaryValue forChar(final char symbol) {
      if (symbol == Zero.value) {
        return Zero;
      } else if (symbol == One.value) {
        return One;
      } else {
        throw new RuntimeException(
            String.format("%s is not a valid symbol for a binary string", symbol));
      }
    }
    
    private final char value;
    
    BinaryValue(final char value) {
      this.value = value;
    }
    
    private BinaryValue invert() {
      return this.equals(Zero) ? One : Zero;
    }
    
  }
  
  private abstract class RunLengthCoder {
    
    int getOccurrences(final char symbol, final int start) {
      int i = start + 1;
      while (i < inputLength && symbol == input.charAt(i)) {
        i++;
      }
      
      return i - start;
    }
  
    int intValue(final char symbol) throws NumberFormatException {
      return Integer.parseInt(String.valueOf(symbol));
    }
    
    protected abstract String encode();
    
    protected abstract String decode();
    
  }
  
  private class RunLengthCoderImpl extends RunLengthCoder {
    
    @Override
    public String encode() {
      final StringBuilder resultBuilder = new StringBuilder();
      int i = 0;
      while (i < inputLength) {
        final char currentSymbol = input.charAt(i);
        int occurrences = getOccurrences(currentSymbol, i);
        resultBuilder.append(currentSymbol);
        i += occurrences;
  
        // add occurrences with respect of max allowed occurrences before an empty symbol switch is required
        while (occurrences > config.maxOccurrences) {
          resultBuilder.append(config.maxOccurrences).append(currentSymbol);
          occurrences -= config.maxOccurrences;
        }
        
        if (occurrences > 1) {
          resultBuilder.append(occurrences);
        }
      }
      
      return resultBuilder.toString();
    }
    
    @Override
    public String decode() {
      final StringBuilder resultBuilder = new StringBuilder();
      for (int i = 0; i < inputLength; i++) {
        final char currentSymbol = input.charAt(i);
        int occurrences = 1;
        if (i + 1 < inputLength) {
          try {
            occurrences = intValue(input.charAt(i + 1));
            i++; // skip coded number of occurrences
          } catch (final NumberFormatException e) {
            // next symbol is not a number --> only one occurrence
          }
        }
        
        while (occurrences-- > 0) {
          resultBuilder.append(currentSymbol);
        }
      }
      
      return resultBuilder.toString();
    }
    
  }
  
  private class BinaryRunLengthCoderImpl extends RunLengthCoder {
    
    @Override
    public String encode() {
      final StringBuilder resultBuilder = new StringBuilder();
      int i = 0;
      while (i < inputLength) {
        final BinaryValue currentSymbol = BinaryValue.forChar(input.charAt(i));
        int occurrences = getOccurrences(currentSymbol.value, i);
        if (i == 0 && currentSymbol != config.binaryStart) { // skip start symbol if first input-symbol is not equal
          resultBuilder.append(0);
        }
  
        i += occurrences;
        
        // add occurrences with respect of max allowed occurrences before an empty symbol switch is required
        while (occurrences > config.maxOccurrences) {
          resultBuilder.append(config.maxOccurrences).append(0);
          occurrences -= config.maxOccurrences;
        }
        
        if (occurrences > 0) {
          resultBuilder.append(occurrences);
        }
      }
      
      return resultBuilder.toString();
    }
    
    @Override
    public String decode() {
      final StringBuilder resultBuilder = new StringBuilder();
      BinaryValue currentSymbol = config.binaryStart;
      for (int i = 0; i < inputLength; i++) {
        int occurrences = intValue(input.charAt(i));
        while (occurrences-- > 0) {
          resultBuilder.append(currentSymbol.value);
        }
        currentSymbol = currentSymbol.invert();
      }
      
      return resultBuilder.toString();
    }
    
  }
  
}
