package com.arrays.demo.observer;

import com.arrays.demo.entity.NumberArray;
import com.arrays.demo.util.Arguments;
import com.arrays.demo.warehouse.Warehouse;

public final class ArrayObserver implements Observer {
  private final Warehouse warehouse;

  public ArrayObserver(Warehouse warehouse) {
    this.warehouse = Arguments.require(warehouse);
  }

  @Override
  public void update(NumberArray array) {
    warehouse.recalculate(Arguments.require(array));
  }
}
