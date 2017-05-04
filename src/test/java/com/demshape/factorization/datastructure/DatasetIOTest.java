package com.demshape.factorization.datastructure;

import org.jblas.FloatMatrix;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DatasetIOTest {

    @Before
    public void before() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void splitToFile() throws IOException {
        String dataset = "3	1	1,5000\n" +
                "1	2	1,5000\n" +
                "1	3	5,5000\n" +
                "1	4	3,5000\n" +
                "1	5	0,5000\n" +
                "3	3	2,5000\n" +
                "3	2	5,5000\n" +
                "1	6	1,5000\n" +
                "1	7	5,5000\n" +
                "1	8	3,5000\n" +
                "1	9	0,5000\n" +
                "3	4	3,5000\n" +
                "2	2	2,5000\n" +
                "3	5	6,5000\n" +
                "3	2	1,5000";
        StringReader input = new StringReader(dataset);
        StringWriter output1 = new StringWriter();
        StringWriter output2 = new StringWriter();
        DatasetIO.splitToFile(input, output1, output2, 0.8);
    }

    @Test
    public void writeLatentVectors() {
        Map<Integer, FloatMatrix> latentVectors = new HashMap<>();
        latentVectors.put(1, new FloatMatrix(new float[]{0.1f, 0.01f}));
        latentVectors.put(2, new FloatMatrix(new float[]{0.2f, 0.02f}));
        StringWriter writer = new StringWriter();
        try {
            DatasetIO.write(latentVectors, writer);
        } catch (IOException e) {

        }
        String expected = String.format("%d %.4f %.4f%n%d %.4f %.4f%n", 1, 0.1, 0.01, 2, 0.2, 0.02);
        assertEquals(writer.toString(), expected);
    }

    @Test
    public void readLatentVectors() throws IOException {
        String input = "10 1.0 2.0 3.0\n20 2.0 3.0 4.0";
        Map<Integer, FloatMatrix> got = DatasetIO.readLatentVectors(new StringReader(input));
        Map<Integer, FloatMatrix> expected = new HashMap<>();
        expected.put(10, new FloatMatrix(new float[]{1, 2, 3}));
        expected.put(20, new FloatMatrix(new float[]{2, 3, 4}));
        assertEquals(expected, got);
    }

    @Test
    public void readCreator() throws IOException {
        String input = "1\t3:1.5\t6:2\n" +
                "2\t3:1\t7:2.3\n";
        StringReader reader = new StringReader(input);
        Map<Integer, SparseVector> got = DatasetIO.readFeatures(reader);
        Map<Integer, SparseVector> expected = new HashMap<>();
        expected.put(1, new SparseVector(new int[]{3, 6}, new float[]{1.5f, 2.0f}));
        expected.put(2, new SparseVector(new int[]{3, 7}, new float[]{1.0f, 2.3f}));
        assertEquals(expected, got);
    }

    @Test
    public void readLineBased() throws IOException {
        String fixture = getClass().getClassLoader().getResource("dataset_line_based.tsv").getPath();
        int[][] got = DatasetIO.readRowBased(fixture);
        int[] extectedLine = new int[]{1, 2, 3};
        int[][] expected = new int[3][];
        expected[0] = extectedLine;
        expected[1] = extectedLine;
        expected[2] = extectedLine;
        assertArrayEquals(expected, got);
    }
}
