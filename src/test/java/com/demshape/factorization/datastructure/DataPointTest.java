package com.demshape.factorization.datastructure;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class DataPointTest {

    @Test
    public void string() {
        DataPoint d = new DataPoint(1, 2, 3);
        String expected = "1\t2\t3.0";
        assertEquals(expected, d.toString());
    }

    @Test
    public void stringConstructor() {
        DataPoint got = new DataPoint("1\t2\t3");
        DataPoint expected = new DataPoint(1, 2, 3);
        assertEquals(expected, got);
    }
}
