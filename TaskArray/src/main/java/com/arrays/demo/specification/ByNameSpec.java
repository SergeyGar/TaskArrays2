package com.arrays.demo.specification;

import com.arrays.demo.entity.NumberArray;
import com.arrays.demo.util.Arguments;

public final class ByNameSpec implements Specification<NumberArray> {
  private final String name;
  public ByNameSpec(String name) { this.name = Arguments.require(name); }
  public boolean isSatisfiedBy(NumberArray item) {
    return name.equals(Arguments.require(item).getName());
  }
}
