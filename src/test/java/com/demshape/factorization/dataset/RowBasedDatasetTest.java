package com.demshape.factorization.dataset;

import com.demshape.factorization.datastructure.DataPoint;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RowBasedDatasetTest {

    private RowBasedDataset dataset;

    @Before
    public void init() {
        int[][] lines = new int[3][];
        lines[0] = new int[]{0, 2, 4, 6};
        lines[1] = new int[]{8, 10, 12};
        lines[2] = new int[]{0, 2};
        dataset = new RowBasedDataset(lines, 2, 1);
    }

    @Test
    public void computeSize() {
        assertEquals(dataset.size(), 14);
    }

    @Test
    public void iterator() {
        Iterator<DataPoint> iterator = dataset.iterator();

        assertEquals(new DataPoint(3, 0, 1.0f), iterator.next());
        assertEquals(new DataPoint(5, 0, 1.0f), iterator.next());
        assertEquals(new DataPoint(1, 2, 1.0f), iterator.next());
        assertEquals(new DataPoint(5, 2, 1.0f), iterator.next());
        assertEquals(new DataPoint(3, 4, 1.0f), iterator.next());
        assertEquals(new DataPoint(7, 4, 1.0f), iterator.next());
        assertEquals(new DataPoint(5, 6, 1.0f), iterator.next());
        assertEquals(new DataPoint(11, 8, 1.0f), iterator.next());
        assertEquals(new DataPoint(9, 10, 1.0f), iterator.next());
        assertEquals(new DataPoint(13, 10, 1.0f), iterator.next());
        assertEquals(new DataPoint(9, 12, 1.0f), iterator.next());
        assertEquals(new DataPoint(11, 12, 1.0f), iterator.next());
        assertEquals(new DataPoint(3, 0, 1.0f), iterator.next());
        assertEquals(new DataPoint(1, 2, 1.0f), iterator.next());
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
    }


}
