package io.github.phantamanta44.libnine.util.collection;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public interface ISieve<T> extends Iterable<T> {

    T get(int index);

    ISieve<T> excluding(int index);

    int size();

    static <T> ISieve<T> over(List<T> backing) {
        return new Impl<>(backing);
    }

    class Impl<T> implements ISieve<T> {

        private final List<T> backing;

        Impl(List<T> backing) {
            this.backing = backing;
        }

        @Override
        public T get(int index) {
            return backing.get(index);
        }

        @Override
        public ISieve<T> excluding(int index) {
            return new ExclusionVisor<>(this, index);
        }

        @Override
        public int size() {
            return backing.size();
        }

        @Override
        public Iterator<T> iterator() {
            return new SieveIterator<>(this);
        }

        private static class ExclusionVisor<T> implements ISieve<T> {

            private final ISieve<T> backing;
            private final int holeIndex;

            ExclusionVisor(ISieve<T> backing, int index) {
                if (index < 0 || index >= backing.size()) throw new IndexOutOfBoundsException();
                this.backing = backing;
                this.holeIndex = index;
            }

            @Override
            public T get(int index) {
                return index >= holeIndex ? backing.get(index + 1) : backing.get(index);
            }

            @Override
            public ISieve<T> excluding(int index) {
                return new ExclusionVisor<>(this, index);
            }

            @Override
            public int size() {
                return backing.size() - 1;
            }

            @Override
            public Iterator<T> iterator() {
                return new SieveIterator<>(this);
            }

        }

        private static class SieveIterator<T> implements Iterator<T> {

            private final ISieve<T> sieve;

            private int index;

            SieveIterator(ISieve<T> sieve) {
                this.sieve = sieve;
                this.index = 0;
            }

            @Override
            public boolean hasNext() {
                return index >= sieve.size();
            }

            @Override
            public T next() {
                try {
                    return sieve.get(index++);
                } catch (IndexOutOfBoundsException e) {
                    throw new NoSuchElementException();
                }
            }

        }

    }

}
