package com.arrays.demo.specification;

import com.arrays.demo.entity.NumberArray;
import com.arrays.demo.repository.ArrayRepository;
import com.arrays.demo.util.Arguments;
import com.arrays.demo.warehouse.Warehouse;

public final class ByMetricSpec implements Specification<NumberArray> {
  private final Metric metric;
  private final Comparison comparison;
  private final long integralThreshold;
  private final double averageThreshold;

  public ByMetricSpec(Metric metric, Comparison comparison, long threshold) {
    this.metric = Arguments.require(metric);
    this.comparison = Arguments.require(comparison);
    if (metric == Metric.AVG) {
      throw new IllegalArgumentException("AVG requires a double threshold");
    }
    integralThreshold = threshold;
    averageThreshold = 0;
  }

  public ByMetricSpec(Metric metric, Comparison comparison, double threshold) {
    this.metric = Arguments.require(metric);
    this.comparison = Arguments.require(comparison);
    if (metric != Metric.AVG || !Double.isFinite(threshold)) {
      throw new IllegalArgumentException("Finite double threshold requires AVG");
    }
    averageThreshold = threshold;
    integralThreshold = 0;
  }

  public boolean isSatisfiedBy(NumberArray item) {
    Arguments.require(item);
    if (ArrayRepository.getInstance().findById(item.getId()).orElse(null) != item) {
      return false;
    }
    return Warehouse.getInstance().getStats(item.getId()).map(stats -> {
      int result;
      if (metric == Metric.AVG) {
        result = Double.compare(stats.getAvg(), averageThreshold);
      } else {
        long value = switch (metric) {
          case SUM -> stats.getSum();
          case MAX -> stats.getMax();
          case MIN -> stats.getMin();
          case COUNT -> stats.getCount();
          case AVG -> throw new IllegalStateException("AVG handled separately");
        };
        result = Long.compare(value, integralThreshold);
      }
      return switch (comparison) {
        case GREATER -> result > 0;
        case LESS -> result < 0;
        case EQUAL -> result == 0;
      };
    }).orElse(false);
  }
}
