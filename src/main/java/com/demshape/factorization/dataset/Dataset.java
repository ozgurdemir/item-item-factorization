package com.demshape.factorization.dataset;

import com.demshape.factorization.datastructure.DataPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link DatasetInterface} implementation were all data points are hold as a list of objects in memory.
 * This should only be used for small data sets as each java object has a high memory overhead.
 */
public class Dataset implements DatasetInterface {

    private List<DataPoint> dataPoints;

    public Dataset(int size) {
        dataPoints = new ArrayList<>(size);
    }

    public Dataset(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    @Override
    public boolean addDatapoint(DataPoint dataPoint) {
        return dataPoints.add(dataPoint);
    }

    @Override
    public Iterator<DataPoint> iterator() {
        return dataPoints.iterator();
    }

    @Override
    public long size() {
        return dataPoints.size();
    }

}
