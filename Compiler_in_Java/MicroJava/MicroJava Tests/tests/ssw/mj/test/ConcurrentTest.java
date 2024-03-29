package ssw.mj.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcurrentTest extends BaseCompilerTestCase {
  private static final int NUM_THREADS = 10;
  private static final int NUM_REPEATS = 50;

  private Thread[] threads;
  private List<Throwable> errors;

  private class TestRunnable implements Runnable {
    public void run() {
      try {
        for (int i = 0; i < NUM_REPEATS; i++) {
          SymbolTableTest t = new SymbolTableTest();
          t.setUp();
          t.scriptExample();
        }
      } catch (Throwable ex) {
        errors.add(ex);
      }
    }
  }

  @BeforeEach
  public void setUp() {
    errors = Collections.synchronizedList(new ArrayList<Throwable>());
    threads = new Thread[NUM_THREADS];
    for (int i = 0; i < NUM_THREADS; i++) {
      threads[i] = new Thread(new TestRunnable());
    }
  }

  @Test
  public void concurrentCompilation() throws InterruptedException {
    for (Thread thread : threads) {
      thread.start();
    }
    for (Thread thread : threads) {
      thread.join();
    }

    if (errors.size() > 0) {
      for (Throwable ex : errors) {
        System.err.println("** Exception during concurrent execution");
        ex.printStackTrace();
      }
      assertEquals(0, errors.size(), "exceptions occured");
    }
  }
}
