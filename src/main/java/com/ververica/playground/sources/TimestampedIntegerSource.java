package com.ververica.playground.sources;

import scala.Tuple2;

import java.util.SplittableRandom;

/**
 * A simple random integer generator with data rate throttling logic and event timestamps that may
 * be out of order.
 */
public class TimestampedIntegerSource extends ParallelBaseGenerator<Tuple2<Long, Integer>> {
  private static final long serialVersionUID = 1L;
  private long maxOutOfOrderness;

  public TimestampedIntegerSource(int maxRecordsPerSecond, long maxOutOfOrderness) {
    super(maxRecordsPerSecond);
    this.maxOutOfOrderness = maxOutOfOrderness;
  }

  @Override
  protected Tuple2<Long, Integer> randomEvent(SplittableRandom rnd, long id) {
    return new Tuple2<>(
        System.currentTimeMillis() - rnd.nextLong(maxOutOfOrderness + 1), rnd.nextInt());
  }
}
