package com.demshape.factorization.datastructure;

import com.google.common.base.Splitter;

import java.util.Iterator;

/**
 * Class representing a single data point of a column based input file.
 */
public class DataPoint {
    public int contextId;
    public int itemId;
    public float rating;
    private final static String sep = "\t";
    private final static Splitter splitter = Splitter.on('\t');

    public DataPoint() {
        super();
    }

    public DataPoint(int contextId, int itemId, float rating) {
        super();
        this.contextId = contextId;
        this.itemId = itemId;
        this.rating = rating;
    }

    /**
     * Create a data point from a input line.
     * @param line Line in column based format (tab separated)
     */
    public DataPoint(String line) {
        super();
        Iterator<String> i = splitter.split(line).iterator();
        this.contextId = Integer.parseInt(i.next());
        this.itemId = Integer.parseInt(i.next());
        this.rating = Float.parseFloat(i.next());
    }

    /**
     * Returns a sparse vector representing this data point
     * @param features Additional features to be merged into the output sparse vector.
     * @return A sparse vector where the first two dimensions hold the context and item id. Additional input features
     * are merged into this.
     */
    public SparseVector toSparseVector(SparseVector features) {
        if (features == null)
            return toSparseVector();
        SparseVector sparseVector = new SparseVector(2 + features.size());
        sparseVector.set(0, contextId, 1.0f);
        sparseVector.set(1, itemId, 1.0f);
        int index = 2;
        for (SparseVector.SparseEntry entry : features)
            sparseVector.set(index++, entry.key, entry.value);

        return sparseVector;
    }

    /**
     * Returns a sparse vector representing this data point
     * @return A sparse vector where the first two dimensions hold the context and item id.
     */
    public SparseVector toSparseVector() {
        SparseVector sparseVector = new SparseVector(2);
        sparseVector.set(0, contextId, 1.0f);
        sparseVector.set(1, itemId, 1.0f);
        return sparseVector;
    }

    @Override
    public String toString() {
        return contextId + sep + itemId + sep + rating;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (!(that instanceof DataPoint))
            return false;
        DataPoint d = (DataPoint) that;
        return (contextId == d.contextId && itemId == d.itemId && rating == d.rating);
    }

}
