package at.sschmid.hcc.sbv1.utility;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Utility {
  
  private static final double LOG_FOR_TWO = Math.log(2);
  
  public static int threadCount() {
    return Runtime.getRuntime().availableProcessors() * 2;
  }
  
  public static ExecutorService threadPool() {
    return Executors.newFixedThreadPool(Utility.threadCount());
  }
  
  public static void wait(final ExecutorService executor) {
    executor.shutdown();
    while (!executor.isTerminated()) {
      // wait
    }
  }
  
  public static double binLog(double x) {
    return x != 0 ? Math.log(x) / LOG_FOR_TWO : 0;
  }
  
}
