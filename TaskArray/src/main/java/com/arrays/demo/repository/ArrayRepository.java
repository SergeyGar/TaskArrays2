package com.arrays.demo.repository;

import com.arrays.demo.entity.NumberArray;
import com.arrays.demo.observer.ArrayObserver;
import com.arrays.demo.specification.Specification;
import com.arrays.demo.util.Arguments;
import com.arrays.demo.warehouse.Warehouse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Lazily initialized singleton for single-threaded use. */
public final class ArrayRepository implements Repository {
  private static ArrayRepository instance;
  private final Map<String, NumberArray> arrays = new HashMap<>();
  private final Map<String, ArrayObserver> subscriptions = new HashMap<>();
  private final Warehouse warehouse = Warehouse.getInstance();

  private ArrayRepository() {}

  public static ArrayRepository getInstance() {
    if (instance == null) {
      instance = new ArrayRepository();
    }
    return instance;
  }

  @Override
  public void add(NumberArray array) {
    Arguments.require(array);
    if (arrays.containsKey(array.getId())) {
      throw new IllegalArgumentException("Duplicate array ID: " + array.getId());
    }
    ArrayObserver observer = new ArrayObserver(warehouse);
    arrays.put(array.getId(), array);
    subscriptions.put(array.getId(), observer);
    array.addObserver(observer);
    warehouse.recalculate(array);
  }

  @Override
  public void remove(String id) {
    Arguments.require(id);
    NumberArray array = arrays.get(id);
    if (array != null) {
      array.removeObserver(subscriptions.remove(id));
      arrays.remove(id);
      warehouse.remove(id);
    }
  }

  @Override
  public Optional<NumberArray> findById(String id) {
    return Optional.ofNullable(arrays.get(Arguments.require(id)));
  }

  @Override
  public List<NumberArray> find(Specification<NumberArray> specification) {
    Arguments.require(specification);
    List<NumberArray> result = new ArrayList<>();
    for (NumberArray array : arrays.values()) {
      if (specification.isSatisfiedBy(array)) {
        result.add(array);
      }
    }
    return result;
  }

  @Override
  public List<NumberArray> sort(Comparator<NumberArray> comparator) {
    Arguments.require(comparator);
    List<NumberArray> result = getAll();
    result.sort(comparator);
    return result;
  }

  @Override
  public List<NumberArray> getAll() { return new ArrayList<>(arrays.values()); }

  void reset() {
    for (NumberArray array : getAll()) {
      remove(array.getId());
    }
  }
}
