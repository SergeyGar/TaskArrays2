package com.arrays.demo.specification;

import com.arrays.demo.entity.NumberArray;
import com.arrays.demo.util.Arguments;

public final class ByIdSpec implements Specification<NumberArray> {
  private final String id;
  public ByIdSpec(String id) { this.id = Arguments.require(id); }
  public boolean isSatisfiedBy(NumberArray item) {
    return id.equals(Arguments.require(item).getId());
  }
}
