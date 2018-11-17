package at.sschmid.hcc.sbv1.utility;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utility {
  
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
  
}
