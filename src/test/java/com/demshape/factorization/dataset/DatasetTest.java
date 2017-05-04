package com.demshape.factorization.dataset;

import com.demshape.factorization.datastructure.DataPoint;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class DatasetTest {
	
	private Dataset dataset;
	
	@Before
	public void before() throws Exception {
		dataset = new Dataset(5);
	}

	@Test
	public void addDatapoint() {
		dataset.addDatapoint(new DataPoint(1, 10, 0.5f));
		dataset.addDatapoint(new DataPoint(1, 11, 0.5f));
		dataset.addDatapoint(new DataPoint(2, 12, 0.5f));
		dataset.addDatapoint(new DataPoint(2, 11, 0.5f));
		
		assertEquals(dataset.size(), 4);
	}
}
