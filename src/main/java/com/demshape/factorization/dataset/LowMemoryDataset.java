package com.demshape.factorization.dataset;

import java.util.Iterator;

import com.demshape.factorization.datastructure.DataPoint;

/**
 * A {@link DatasetInterface} implementation which is internally based on plain
 * java arrays in order to use less memory.
 */
public class LowMemoryDataset implements DatasetInterface {

    private int[] contextIDs;
    private int[] itemIDs;
    private float[] ratings;
    private int size;

    public LowMemoryDataset(int capacity) {
        contextIDs = new int[capacity];
        itemIDs = new int[capacity];
        ratings = new float[capacity];
        this.size = 0;
    }

    @Override
    public Iterator<DataPoint> iterator() {
        return new LowMemoryDatasetIterator();
    }

    @Override
    public boolean addDatapoint(DataPoint dataPoint) {
        contextIDs[size] = dataPoint.contextId;
        itemIDs[size] = dataPoint.itemId;
        ratings[size] = dataPoint.rating;
        size++;
        return true;
    }

    @Override
    public long size() {
        return size;
    }


    private class LowMemoryDatasetIterator implements Iterator<DataPoint> {

        private int index;

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public DataPoint next() {
            DataPoint dataPoint = new DataPoint(contextIDs[index], itemIDs[index], ratings[index]);
            ++index;
            return dataPoint;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}