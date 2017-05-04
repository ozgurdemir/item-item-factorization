package com.demshape.factorization.metric;

import java.security.InvalidParameterException;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Computes binned accuracy (percentage of correct positive and negative predictions) based on item occurrences in
 * the train set.
 * Long tail items are expected to have a worse prediction than items with lots of signals.
 */
public class Accuracy {

    private int[] bins;
    private AtomicIntegerArray pos;
    private AtomicIntegerArray posCorrect;
    private AtomicIntegerArray neg;
    private AtomicIntegerArray negCorrect;

    public Accuracy(int[] bins) {
        this.bins = bins;
        pos = new AtomicIntegerArray(bins.length);
        posCorrect = new AtomicIntegerArray(bins.length);
        neg = new AtomicIntegerArray(bins.length);
        negCorrect = new AtomicIntegerArray(bins.length);
    }

    public void addPos(float value, double threshold, int count) {
        final int bin = bin(count);
        pos.incrementAndGet(bin);
        if (value > threshold)
            posCorrect.incrementAndGet(bin);
    }

    public void addNeg(float value, double threshold, int count) {
        final int bin = bin(count);
        neg.incrementAndGet(bin);
        if (value < threshold)
            negCorrect.incrementAndGet(bin);
    }

    public double average() {
        int sum = 0;
        int sumCorrect = 0;
        for (int i = 0; i < bins.length; i++) {
            sum += pos.get(i) + neg.get(i);
            sumCorrect += posCorrect.get(i) + negCorrect.get(i);
        }
        return 100.0 * sumCorrect / (double) sum;
    }

    public String toString() {
        StringBuffer strBuffer = new StringBuffer();
        double average = average();
        strBuffer.append(String.format("%.2f%% --> ", average));
        for (int i = 0; i < bins.length; i++) {
            double positivePercentage = 100.0 * posCorrect.get(i) / (double) pos.get(i);
            double negativePercentage = 100.0 * negCorrect.get(i) / (double) neg.get(i);
            strBuffer.append(String.format("bin-%d: p:%.2f%%(%d) n:%.2f%%(%d) | ", bins[i], positivePercentage, pos.get(i), negativePercentage, neg.get(i)));
        }
        return strBuffer.toString();


    }

    public int bin(int count) throws InvalidParameterException {
        for (int i = 0; i < bins.length; i++) {
            if (count < bins[i])
                return i;
        }
        throw new InvalidParameterException(String.format("%d is outside the bins", count));
    }

}
