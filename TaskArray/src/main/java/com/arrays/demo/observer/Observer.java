package com.arrays.demo.observer;

import com.arrays.demo.entity.NumberArray;

public interface Observer {
  void update(NumberArray array);
}
