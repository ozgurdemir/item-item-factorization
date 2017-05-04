package com.demshape.factorization.dataset;

import com.demshape.factorization.datastructure.DataPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Random;

/**
 * A {@link DatasetInterface} implementation which uses a row based input format.
 * Pairs of co-occurrences are generated on the fly while streaming through the
 * data set. The pairs are generated based on the window size.
 */
public class RowBasedDataset implements DatasetInterface, Iterable<DataPoint> {

    private static final Logger logger = LogManager.getLogger(RowBasedDataset.class);

    private int[][] lines;
    private long size;
    private int window;
    private Long seed;

    public RowBasedDataset(int[][] lines, int window, long seed) {
        this.lines = lines;
        this.window = window;
        this.seed = seed;
        computeSize();
    }

    // todo: pre-compute this instead of iterating
    private void computeSize() {
        new WindowIterator().forEachRemaining(dataPoint -> size++);
        logger.info("Number of positive data points:" + size);
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public boolean addDatapoint(DataPoint dataPoint) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public Iterator<DataPoint> iterator() {
        return new WindowIterator();
    }

    private class WindowIterator implements Iterator<DataPoint> {
        private int line;
        private int context;
        private int item;
        private int[] currentLine;
        private int sampledWindow;
        private Random random;

        public WindowIterator() {
            item = 0;
            context = 1;
            currentLine = lines[0];
            random = new Random(seed);
            sampledWindow = random.nextInt(window) + 1;
        }

        @Override
        public boolean hasNext() {
            return line < lines.length;
        }

        @Override
        public DataPoint next() {
            DataPoint dataPoint = new DataPoint(currentLine[context] + 1, currentLine[item], 1.0f);
            // jump to next context
            ++context;
            // if context == item jump to next context
            if (context == item)
                ++context;
            // if end of window jump to next item
            if (context - item > sampledWindow || context == currentLine.length) {
                ++item;
                sampledWindow = random.nextInt(window) + 1;
                context = Math.max(0, item - sampledWindow);
            }
            // if end of line jump to next line
            if (item == currentLine.length && ++line < lines.length) {
                currentLine = lines[line];
                item = 0;
                context = 1;
                sampledWindow = random.nextInt(window) + 1;
            }
            return dataPoint;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
