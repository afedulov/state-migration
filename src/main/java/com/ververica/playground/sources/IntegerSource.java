package com.ververica.playground.sources;

import java.util.SplittableRandom;

/** A simple random integer generator with data rate throttling logic. */
public class IntegerSource extends ParallelBaseGenerator<Integer> {
  private static final long serialVersionUID = 1L;

  private int limit = Integer.MAX_VALUE;

  public IntegerSource(int maxRecordsPerSecond) {
    super(maxRecordsPerSecond);
  }

  public IntegerSource(int maxRecordsPerSecond, int limit) {
    super(maxRecordsPerSecond);
    this.limit = limit;
  }

  @Override
  protected Integer randomEvent(SplittableRandom rnd, long id) {
    return rnd.nextInt(limit);
  }
}
