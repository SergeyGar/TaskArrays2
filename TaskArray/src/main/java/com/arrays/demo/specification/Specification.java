package com.arrays.demo.specification;

@FunctionalInterface
public interface Specification<T> {
  boolean isSatisfiedBy(T item);
}
