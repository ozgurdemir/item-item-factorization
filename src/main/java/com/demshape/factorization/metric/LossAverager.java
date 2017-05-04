package com.demshape.factorization.metric;

import java.security.InvalidParameterException;

/**
 * Helper class to compute binned averages.
 * It is used to compute binned prediction errors based on item occurrences in the train set.
 * Long tail items are expected to have worse predictions than items with lots of signals.
 */
public class LossAverager {

    /**
     * The bin boundaries
     */
    private int[] bins;

    /**
     * Number of elements per bin
     */
    protected long[] binCounts;

    /**
     * Summed loss per bin
     */
    protected double[] binnedLoss;

    public LossAverager(int[] bins) {
        this.bins = bins;
        binCounts = new long[bins.length];
        binnedLoss = new double[bins.length];
    }

    /**
     * Adds a loss for a particular item.
     * @param loss loss to be count
     * @param count the number of times this item occurs in the train set
     */
    public void add(double loss, int count) {
        final int bin = bin(count);
        binnedLoss[bin] += loss;
        binCounts[bin]++;
    }

    /**
     * Sum up two lossAveragers
     * @param lossAverager Another lossAverager
     * @return Merged lossAverager
     */
    public LossAverager add(LossAverager lossAverager) {
        for (int i = 0; i < bins.length; i++) {
            binCounts[i] += lossAverager.binCounts[i];
            binnedLoss[i] += lossAverager.binnedLoss[i];
        }
        return this;
    }

    /**
     * @return Averaged loss over all bins
     */
    public double average() {
        double sumLoss = 0.0;
        long sumCount = 0;
        for (int i = 0; i < bins.length; i++) {
            sumLoss += binnedLoss[i];
            sumCount += binCounts[i];
        }
        return sumLoss / sumCount;
    }

    /**
     * @return Averages loss per bin
     */
    public double[] binnedLoss() {
        double[] result = new double[bins.length];
        for (int i = 0; i < bins.length; i++)
            result[i] = binnedLoss[i] / binCounts[i];
        return result;
    }

    public String toString() {
        double average = average();
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append(String.format("%.4f --> ", average));
        for (int i = 0; i < bins.length; i++) {
            final double loss = binnedLoss[i] / binCounts[i];
            strBuffer.append(String.format("bin-%d:%.4f (%d) | ", bins[i], loss, binCounts[i]));
        }
        return strBuffer.toString();
    }

    /**
     * @param count number of times an item occurred in train set
     * @return bin index for a particular count
     * @throws InvalidParameterException If number is larger than max bin
     */
    public int bin(int count) throws InvalidParameterException {
        for (int i = 0; i < bins.length; i++) {
            if (count < bins[i])
                return i;
        }
        throw new InvalidParameterException(String.format("%d is outside the bins", count));
    }

}
