package com.arrays.demo.repository;

import com.arrays.demo.entity.NumberArray;
import com.arrays.demo.specification.Specification;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public interface Repository {
  void add(NumberArray array);
  void remove(String id);
  Optional<NumberArray> findById(String id);
  List<NumberArray> find(Specification<NumberArray> specification);
  List<NumberArray> sort(Comparator<NumberArray> comparator);
  List<NumberArray> getAll();
}
