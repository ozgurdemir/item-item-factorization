package com.demshape.factorization.dataset;

import com.demshape.factorization.datastructure.DataPoint;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Interface which defines a column based data set.
 */
public interface DatasetInterface extends Iterable<DataPoint> {

    /**
     * Add a data point to the current data set.
     * @param dataPoint The data point to be added.
     * @return True if added successfully otherwise false
     */
    boolean addDatapoint(DataPoint dataPoint);

    /**
     * @return Number of data points in this data set
     */
    long size();

    /**
     * A default implementation which returns a parallel stream. This can be used to concurrently stream over the data
     * set.
     * @return A parallel stream over the data points of this data set.
     */
    default Stream<DataPoint> parallelStream() {
        boolean parallel = true;
        Spliterator<DataPoint> spliterator = Spliterators.spliterator(iterator(), size(), characteristics());
        return StreamSupport.stream(spliterator, parallel);
    }

    /**
     * Stream characteristics.
     * Distinct: each element is distinct
     * Sized: it is known how many elments this list contains
     * Nonnull: none of the elements are null
     * Immutable: the elements are immutable
     * Subsized:
     * @return A int of the characteristics.
     */
    static int characteristics() {
        return Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.SUBSIZED;
    }

}
