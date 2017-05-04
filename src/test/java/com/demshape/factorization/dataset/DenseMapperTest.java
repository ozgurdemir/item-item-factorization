package com.demshape.factorization.dataset;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DenseMapperTest {

    private DenseMapper denseMapper;

    @Before
    public void before() {
        denseMapper = new DenseMapper();
    }

    @Test
    public void detect() {
        int[] line = new int[]{1, 2, 3};
        int[][] lines = new int[3][];
        lines[0] = line;
        lines[1] = line;
        lines[2] = line;
        Set<Integer> expected = new HashSet<Integer>(Arrays.asList(1, 2, 3));
        denseMapper.detect(lines);
        assertEquals(denseMapper.contexts, expected);
        assertEquals(denseMapper.items, expected);
    }

    @Test
    public void createMap() {
        Set<Integer> set = new HashSet<Integer>(Arrays.asList(1, 2, 3));
        denseMapper.createMap(set, set, set);

        Map<Integer, Integer> contextMap = new HashMap<>();
        contextMap.put(1, 1);
        contextMap.put(2, 3);
        contextMap.put(3, 5);
        assertEquals(contextMap, denseMapper.contextMap);

        Map<Integer, Integer> itemMap = new HashMap<>();
        itemMap.put(1, 0);
        itemMap.put(2, 2);
        itemMap.put(3, 4);
        assertEquals(itemMap, denseMapper.itemMap);

        Map<Integer, Integer> featureMap = new HashMap<>();
        featureMap.put(1, 6);
        featureMap.put(2, 7);
        featureMap.put(3, 8);
        assertEquals(featureMap, denseMapper.featureMap);
    }

    @Test
    public void map() {
        int[][] lines = new int[3][3];
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                lines[i][j] = j + 1;
        Map<Integer, Integer> itemMap = new HashMap<>();
        itemMap.put(1, 0);
        itemMap.put(2, 1);
        itemMap.put(3, 2);
        denseMapper.itemMap = itemMap;
        denseMapper.map(lines);
        int[] mappedLine = new int[]{0, 1, 2};
        int[][] mappedLines = new int[3][];
        mappedLines[0] = mappedLine;
        mappedLines[1] = mappedLine;
        mappedLines[2] = mappedLine;
        assertArrayEquals(mappedLines, lines);
    }
}