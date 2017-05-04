package com.demshape.factorization.datastructure;

import org.jblas.FloatMatrix;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DatasetGeneratorTest  {

    @Test
    public void sumLatentVectors() {
        Map<Integer, FloatMatrix> latentA = new HashMap<>();
        latentA.put(1, new FloatMatrix(new float[]{1, 3, 4}));
        latentA.put(2, new FloatMatrix(new float[]{2, 2, 4}));
        latentA.put(3, new FloatMatrix(new float[]{1, 3, 4}));

        Map<Integer, FloatMatrix> latentB = new HashMap<>();
        latentB.put(1, new FloatMatrix(new float[]{3, 3, 5}));
        latentB.put(2, new FloatMatrix(new float[]{2, 2, 4}));

        Map<Integer, FloatMatrix> got = DatasetGenerator.sumLatentVectors(latentA, latentB);
        Map<Integer, FloatMatrix> expected = new HashMap<>();
        expected.put(1, new FloatMatrix(new float[]{4, 6, 9}));
        expected.put(2, new FloatMatrix(new float[]{4, 4, 8}));

        assertEquals(expected, got);
    }

}
