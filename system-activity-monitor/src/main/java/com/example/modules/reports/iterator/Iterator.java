package com.example.modules.reports.iterator;

public interface Iterator<T> {
    void first();
    void next();
    boolean isDone();
    T currentItem();
}

