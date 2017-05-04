package com.demshape.factorization.dataset;

import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.demshape.factorization.datastructure.DataPoint;

/**
 * A {@link DatasetInterface} implementation where data points are streamed from disk. It is hence suited
 * for very large data sets which do not fit into main memory.
 */
public class DiskBasedDataset implements DatasetInterface, Iterable<DataPoint> {

    private String input;
    private int size;
    private BufferedWriter writer;

    final private String lineBreak = System.lineSeparator();

    public DiskBasedDataset(String input, int size) {
        this.input = input;
        this.size = size;
    }

    @Override
    public Iterator<DataPoint> iterator() {
        return new DiskBasedDatasetIterator(input);
    }

    @Override
    public boolean addDatapoint(DataPoint dataPoint) {
        try {
            if (writer == null)
                writer = new BufferedWriter(new FileWriter(input));
            writer.append(dataPoint.toString());
            writer.append(lineBreak);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        return true;
    }

    public void closeStream() throws IOException {
        writer.close();
        writer = null;
    }

    public long size() {
        return size;
    }

}

class DiskBasedDatasetIterator implements Iterator<DataPoint> {

    private String nextLine;
    private BufferedReader bufReader;

    public DiskBasedDatasetIterator(String input) {
        try {
            bufReader = new BufferedReader(new FileReader(input));
            nextLine = bufReader.readLine();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return nextLine != null;
    }

    @Override
    public DataPoint next() {
        if (nextLine == null)
            throw new NoSuchElementException();
        try {
            String currentLine = nextLine;
            nextLine = bufReader.readLine();
            return new DataPoint(currentLine);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
