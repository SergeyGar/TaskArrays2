package com.arrays.demo.factory;

import com.arrays.demo.entity.NumberArray;
import com.arrays.demo.repository.ArrayRepository;

public final class NumberArrayFactory {
  public NumberArray create(String id, String name, int[] data) {
    NumberArray array = new NumberArray(id, name, data);
    ArrayRepository.getInstance().add(array);
    return array;
  }
}
