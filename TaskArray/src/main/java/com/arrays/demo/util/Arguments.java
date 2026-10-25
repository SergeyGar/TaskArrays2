package com.arrays.demo.util;

public final class Arguments {
  private Arguments() {}

  public static <T> T require(T value) {
    if (value == null) {
      throw new IllegalArgumentException("Argument must not be null");
    }
    return value;
  }
}
