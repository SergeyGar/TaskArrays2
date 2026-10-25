package com.arrays.demo.warehouse;

import com.arrays.demo.entity.NumberArray;
import com.arrays.demo.util.Arguments;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Lazily initialized singleton for single-threaded use. */
public final class Warehouse {
  private static Warehouse instance;
  private final Map<String, Statistics> statistics = new HashMap<>();

  private Warehouse() {}

  public static Warehouse getInstance() {
    if (instance == null) {
      instance = new Warehouse();
    }
    return instance;
  }

  public void recalculate(NumberArray array) {
    Arguments.require(array);
    long sum = 0;
    int min = array.get(0);
    int max = min;
    for (int value : array.snapshot()) {
      sum += value;
      min = Math.min(min, value);
      max = Math.max(max, value);
    }
    statistics.put(array.getId(), new Statistics(sum, (double) sum / array.size(),
        max, min, array.size()));
  }

  public void remove(String id) { statistics.remove(Arguments.require(id)); }
  public Optional<Statistics> getStats(String id) {
    return Optional.ofNullable(statistics.get(Arguments.require(id)));
  }
  void reset() { statistics.clear(); }

  public static final class Statistics {
    private final long sum;
    private final double avg;
    private final int max;
    private final int min;
    private final int count;

    private Statistics(long sum, double avg, int max, int min, int count) {
      this.sum = sum;
      this.avg = avg;
      this.max = max;
      this.min = min;
      this.count = count;
    }

    public long getSum() { return sum; }
    public double getAvg() { return avg; }
    public int getMax() { return max; }
    public int getMin() { return min; }
    public int getCount() { return count; }
  }
}
