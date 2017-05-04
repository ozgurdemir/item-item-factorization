package com.demshape.factorization.dataset;

import com.demshape.factorization.datastructure.DataPoint;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class DiskBasedDatasetTest {
    private DiskBasedDataset dataset;

    @Before
    public void before() throws Exception {
        String fixture = getClass().getClassLoader().getResource("ratings.tsv").getPath();
        dataset = new DiskBasedDataset(fixture, 3);
    }

    @Test
    public void equals() {
        DataPoint d1 = new DataPoint(1, 2, 3);
        DataPoint d2 = new DataPoint(1, 2, 3);
        assertEquals(d1, d2);

    }

    @Test
    public void nextDatapoint() {
        Iterator<DataPoint> iterator = dataset.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(iterator.next(), new DataPoint(1, 22, 4));
        assertEquals(iterator.next(), new DataPoint(1, 23, 3));
        assertEquals(iterator.next(), new DataPoint(1, 24, 8));
        assertFalse(iterator.hasNext());

        // 2nd run
        iterator = dataset.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(iterator.next(), new DataPoint(1, 22, 4));
        assertEquals(iterator.next(), new DataPoint(1, 23, 3));
        assertEquals(iterator.next(), new DataPoint(1, 24, 8));
        assertFalse(iterator.hasNext());
    }

}
