package com.arrays.demo.entity;

import com.arrays.demo.observer.Observer;
import com.arrays.demo.util.Arguments;
import java.util.ArrayList;
import java.util.List;

public final class NumberArray implements Identifiable {
  private final String id;
  private final String name;
  private final int[] data;
  private final List<Observer> observers = new ArrayList<>();

  public NumberArray(String id, String name, int[] data) {
    this.id = Arguments.require(id);
    this.name = Arguments.require(name);
    Arguments.require(data);
    if (data.length == 0) {
      throw new IllegalArgumentException("Array must not be empty");
    }
    this.data = data.clone();
  }

  public String getId() { return id; }
  public String getName() { return name; }
  public int get(int index) { return data[index]; }
  public int size() { return data.length; }
  public int[] snapshot() { return data.clone(); }

  public void set(int index, int value) {
    if (data[index] != value) {
      data[index] = value;
      for (Observer observer : new ArrayList<>(observers)) {
        observer.update(this);
      }
    }
  }

  public void addObserver(Observer observer) {
    Arguments.require(observer);
    if (!observers.contains(observer)) {
      observers.add(observer);
    }
  }

  public void removeObserver(Observer observer) {
    observers.remove(Arguments.require(observer));
  }
}
