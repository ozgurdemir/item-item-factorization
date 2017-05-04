package com.demshape.factorization.datastructure;


import java.util.Collections;
import java.util.Iterator;

/**
 * A sparse vector implementation. Implements an iterator over the entries.
 */
public class SparseVector implements Iterable<SparseVector.SparseEntry> {

    /**
     * The index of each feature
     */
    public int[] keys;

    /**
     * Value of each feature
     */
    public float[] values;

    private static Iterator<SparseEntry> emptyIterator = Collections.emptyIterator();

    public SparseVector(int capacity) {
        assert capacity >= 0;
        this.keys = new int[capacity];
        this.values = new float[capacity];
    }

    public SparseVector(int[] keys, float[] values) {
        this.keys = keys;
        this.values = values;
    }

    public void set(int index, int key, float value) {
        keys[index] = key;
        values[index] = value;
    }

    public SparseEntry get(int index) {
        return new SparseEntry(keys[index], values[index]);
    }

    public int size() {
        return keys.length;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (!(that instanceof SparseVector))
            return false;
        SparseVector d = (SparseVector) that;
        Iterator<SparseEntry> iterA = iterator();
        Iterator<SparseEntry> iterB = d.iterator();

        while (iterA.hasNext()) {
            if (!iterA.next().equals(iterB.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        iterator().forEachRemaining(i -> stringBuffer.append(i + " "));
        return stringBuffer.toString();
    }

    /**
     * A single entry of a sparse vector.
     */
    public static class SparseEntry {
        public int key;
        public float value;

        public SparseEntry(int key, float value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object that) {
            if (this == that)
                return true;
            if (!(that instanceof SparseEntry))
                return false;
            SparseEntry d = (SparseEntry) that;
            return (key == d.key && value == d.value);
        }

        @Override
        public String toString() {
            return key + ":" + value;
        }
    }

    public Iterator<SparseEntry> iterator() {
        if (keys.length == 0)
            return emptyIterator;
        return new SparseEntryIterator();
    }

    private class SparseEntryIterator implements Iterator<SparseEntry> {

        private int index;

        @Override
        public boolean hasNext() {
            return index < keys.length;
        }

        @Override
        public SparseEntry next() {
            SparseEntry sparseEntry = new SparseEntry(keys[index], values[index]);
            ++index;
            return sparseEntry;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}
