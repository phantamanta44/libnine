package io.github.phantamanta44.libnine.util.math;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IntRange extends AbstractSet<Integer> {

    private final int start;
    private final int end;

    public IntRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean isEmpty() {
        return end <= start;
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof Integer && contains(((Integer)o).intValue());
    }

    public boolean contains(int n) {
        return n >= start && n < end;
    }

    @Override
    public boolean add(Integer integer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<Integer> stream() {
        return IntStream.range(start, end).boxed();
    }

    @Override
    public Iterator<Integer> iterator() {
        return new IntRangeIterator(start, end);
    }

    @Override
    public int size() {
        return Math.max(end - start, 0);
    }

    @Override
    public String toString() {
        return String.format("%d..%d", start, end);
    }

    private static class IntRangeIterator implements Iterator<Integer> {

        private final int end;
        private int value;

        public IntRangeIterator(int start, int end) {
            this.value = start;
            this.end = end;
        }

        @Override
        public boolean hasNext() {
            return value < end;
        }

        @Override
        public Integer next() {
            if (value >= end) {
                throw new NoSuchElementException();
            }
            return value++;
        }

    }

}
