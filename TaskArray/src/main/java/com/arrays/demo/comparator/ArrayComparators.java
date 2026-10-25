package com.arrays.demo.comparator;

import com.arrays.demo.entity.NumberArray;
import com.arrays.demo.util.Arguments;
import java.util.Comparator;

public final class ArrayComparators {
  private ArrayComparators() {}

  public static final Comparator<NumberArray> BY_ID =
      (left, right) -> Arguments.require(left).getId()
          .compareTo(Arguments.require(right).getId());
  public static final Comparator<NumberArray> BY_NAME =
      (left, right) -> Arguments.require(left).getName()
          .compareTo(Arguments.require(right).getName());
  public static final Comparator<NumberArray> BY_FIRST_ELEMENT =
      (left, right) -> Integer.compare(Arguments.require(left).get(0),
          Arguments.require(right).get(0));
  public static final Comparator<NumberArray> BY_COUNT =
      (left, right) -> Integer.compare(Arguments.require(left).size(),
          Arguments.require(right).size());
}
