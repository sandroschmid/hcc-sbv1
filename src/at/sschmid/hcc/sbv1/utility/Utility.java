package at.sschmid.hcc.sbv1.utility;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Utility {
  
  private static final double NATURAL_LOG_BASE_TWO = Math.log(2);
  
  public static int threadCount() {
    return Runtime.getRuntime().availableProcessors();
  }
  
  public static ExecutorService threadPool() {
    return threadPool(Utility.threadCount());
  }
  
  public static ExecutorService threadPool(final int threads) {
    return Executors.newFixedThreadPool(threads);
  }
  
  public static <T extends Runnable> void executeAndJoin(final Iterable<T> workers) {
    executeAndJoin(threadPool(), workers);
  }
  
  public static <T extends Runnable> void executeAndJoin(final ExecutorService threadPool,
                                                         final Iterable<T> workers) {
    for (final Runnable worker : workers) {
      threadPool.execute(worker);
    }
    
    join(threadPool);
  }
  
  public static void join(final ExecutorService threadPool) {
    threadPool.shutdown();
    while (!threadPool.isTerminated()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }
  
  public static double binLog(double x) {
    return x != 0 ? Math.log(x) / NATURAL_LOG_BASE_TWO : 0;
  }
  
}
