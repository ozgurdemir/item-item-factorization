package com.demshape.factorization.datastructure;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SparseVectorTest {

    private SparseVector vector;
    private int[] keys = {1, 3, 5};
    private float[] values = {1.0f, -3.0f, 1.0f};

    @Before
    public void before() throws Exception {
        this.vector = new SparseVector(keys, values);
    }

    @Test
    public void set() {
        SparseVector vectorB = new SparseVector(2);
        vectorB.set(0, 1, 2.0f);
        vectorB.set(1, 2, 3.0f);
        Assert.assertArrayEquals(new int[]{1, 2}, vectorB.keys);
        Assert.assertArrayEquals(new float[]{2.0f, 3.0f}, vectorB.values, 0.0f);
    }

    @Test
    public void iterator() {
        Iterator<SparseVector.SparseEntry> sparseEntryIterator = vector.iterator();
        assertEquals(sparseEntryIterator.next(), new SparseVector.SparseEntry(1, 1.0f));
        assertEquals(sparseEntryIterator.next(), new SparseVector.SparseEntry(3, -3.0f));
        assertEquals(sparseEntryIterator.next(), new SparseVector.SparseEntry(5, 1.0f));
        assertFalse(sparseEntryIterator.hasNext());
    }
}
