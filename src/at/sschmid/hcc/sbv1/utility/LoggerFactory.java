package at.sschmid.hcc.sbv1.utility;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerFactory {
  
  private static Level level = Level.INFO;
  
  public static void setLevel(final Level level) {
    LoggerFactory.level = level;
  }
  
  public static Logger getLogger(final String name) {
    final Logger logger = Logger.getLogger(name);
    logger.setLevel(level);
    return logger;
  }
  
  private LoggerFactory() {
    // nothing to do
  }
  
}
