package com.example.modules.reports.iterator;

public interface Aggregate<T> {
    Iterator<T> createIterator();
    int size();
    T get(int index);
}
