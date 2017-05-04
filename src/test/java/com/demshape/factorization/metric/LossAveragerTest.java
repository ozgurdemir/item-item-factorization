package com.demshape.factorization.metric;

import com.demshape.factorization.metric.LossAverager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.security.InvalidParameterException;

import static org.junit.Assert.assertEquals;

public class LossAveragerTest {

    LossAverager lossAverager;

    @Before
    public void before() {
        lossAverager = new LossAverager(new int[]{10, 20, 30, 40});
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void bin() throws InvalidParameterException {
        assertEquals(0, lossAverager.bin(5));
        assertEquals(1, lossAverager.bin(10));
        assertEquals(1, lossAverager.bin(15));
        assertEquals(2, lossAverager.bin(29));
        thrown.expect(InvalidParameterException.class);
        thrown.expectMessage("50 is outside the bins");
        lossAverager.bin(50);
    }
}
