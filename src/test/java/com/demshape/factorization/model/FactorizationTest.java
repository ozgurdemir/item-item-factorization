package com.demshape.factorization.model;

import com.demshape.factorization.dataset.Dataset;
import com.demshape.factorization.dataset.DatasetInterface;
import com.demshape.factorization.datastructure.DataPoint;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class FactorizationTest {

    private Factorization factorization;

    @Before
    public void before() throws Exception {
        factorization = new Factorization();
    }

    @Test
    public void testSamplingTable1() {
        int size = 10;
        int[] distribution = new int[]{0, 0, 30, 0, 40, 0, 0, 20, 10};
        double beta = 1.0;
        factorization.initSamplingTable(distribution, beta, size);
        int[] got = factorization.samplingTable;
        int[] expected = new int[]{2, 2, 2, 4, 4, 4, 4, 7, 7, 8};
        assertArrayEquals(expected, got);
    }

    @Test
    public void testSamplingTable2() {
        int size = 6;
        int[] distribution = new int[]{0, 10, 20, 30, 40, 0, 0};
        double beta = 1.0;
        factorization.initSamplingTable(distribution, beta, size);
        int[] got = factorization.samplingTable;
        int[] expected = new int[]{1, 2, 3, 3, 4, 4};
        assertArrayEquals(expected, got);
    }

    @Test
    public void testSamplingTable3() {
        int size = 10;
        int[] distribution = new int[]{10, 20, 30, 40};
        double beta = 1.0;
        factorization.initSamplingTable(distribution, beta, size);
        int[] got = factorization.samplingTable;
        int[] expected = new int[]{0, 1, 1, 1, 2, 2, 2, 3, 3, 3};
        assertArrayEquals(expected, got);
    }

    @Test
    public void sample() {
        factorization.samplingTable = new int[]{1, 1, 2, 2, 3};
        factorization.tableSize = 5;
        assertThat(factorization.sample(), anyOf(is(1), is(2), is(3)));
    }

    @Test
    public void count() {
        DatasetInterface testset = new Dataset(4);
        testset.addDatapoint(new DataPoint(1, 0, 0));
        testset.addDatapoint(new DataPoint(2, 1, 0));
        testset.addDatapoint(new DataPoint(2, 1, 0));
        testset.addDatapoint(new DataPoint(0, 4, 0));
        factorization.count(5, testset);
        assertArrayEquals(new int[]{1, 1, 2, 0, 0}, factorization.contextCount);
        assertArrayEquals(new int[]{1, 2, 0, 0, 1}, factorization.itemCount);
    }
}
