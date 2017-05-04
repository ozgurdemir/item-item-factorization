package com.demshape.factorization.model;

import com.demshape.factorization.dataset.DatasetInterface;
import com.demshape.factorization.datastructure.DataPoint;
import com.demshape.factorization.datastructure.SparseVector;
import com.demshape.factorization.lossfunction.LossFunctionInterface;
import com.demshape.factorization.metric.LossAverager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jblas.FloatMatrix;

import java.util.concurrent.ExecutionException;

/**
 * An alternative learning function which uses the BPR objective.
 * See 'BPR: Bayesian Personalized Ranking from Implicit Feedback'
 */
public class Bpr extends Factorization {

    private static final Logger logger = LogManager.getLogger(Bpr.class);

    public Bpr(int dimensions, float learnRate, float lambda1, float lambda2, int iterations, int negatives, LossFunctionInterface lossFunction, boolean regression, int tableSize, int minCount, double alpha, int[] bins) {
        super(dimensions, learnRate, lambda1, lambda2, iterations, negatives, lossFunction, regression, tableSize, minCount, alpha, bins);
    }

    @Override
    public void train(DatasetInterface trainset, SparseVector[] optionalFeatures, int numFeatures) throws InterruptedException, ExecutionException {
        initParameters(numFeatures, dimensions);
        initLearnRates(numFeatures);
        count(numFeatures, trainset);
        initSamplingTable(itemCount, alpha, tableSize);

        DataPoint negativeDataPoint = new DataPoint();
        float measured = 1.0f;
        for (int iteration = 1; iteration <= iterations; ++iteration) {
            logger.info(String.format("Iteration %d start...", iteration));
            trainset.parallelStream().map(dataPoint -> {
                LossAverager lossAverager = new LossAverager(bins);
                final int itemOccurrence = itemCount[dataPoint.itemId];

                // positive data point
                SparseVector positiveSparseVector = dataPoint.toSparseVector(optionalFeatures[dataPoint.itemId]);

                // negative data point
                negativeDataPoint.itemId = dataPoint.itemId;
                // todo: does it make a difference which one is sampled?
                dataPoint.itemId = sample();
                SparseVector negativeSparseVector = negativeDataPoint.toSparseVector(optionalFeatures[negativeDataPoint.itemId]);

                float predicted = predict(positiveSparseVector) - predict(negativeSparseVector);
                float loss = lossFunction.g(predicted, measured);
                float weight = dataPoint.rating;
                final float error = weight * lossFunction.gDeriv(predicted, measured);
                lossAverager.add(loss, itemOccurrence);

                trainStep(positiveSparseVector, error, 1.0f);
                trainStep(negativeSparseVector, error, -1.0f);

                return lossAverager;
            }).reduce((a, b) -> a.add(b)).ifPresent(iterationLoss ->
                    logger.info("train loss: " + iterationLoss));
        }
    }

     public float trainStep(SparseVector sparseVector, float error, float group) {
        // pre compute sum of vectors to speed up computation
        final FloatMatrix vectorSum = FloatMatrix.zeros(latentVectors[0].length);
        for (SparseVector.SparseEntry entry : sparseVector)
            vectorSum.addi(latentVectors[entry.key].mul(entry.value));

        // gradient updates
        // XXX(od): do not include quadratic features
        for (SparseVector.SparseEntry entryA : sparseVector) {
            int key = entryA.key;
            float value = entryA.value;
            FloatMatrix vector = latentVectors[key];

            // quadratic term
            final FloatMatrix gradientA = vectorSum.mul(value).subi(vector.mul(value * value));
            gradientA.muli(error * group).addi(vector.mul(lambda2));
            float individualLearnRate = updateLearnRate(key, gradientA);
            vector.subi(gradientA.muli(learnRate * individualLearnRate));

            // linear term
            float current = biases.get(key);
            float linearGradient = value * error * group + lambda1 * current;
            biases.put(key, current - learnRate * individualLearnRate * linearGradient);
        }
        return 0.0f;
    }


}
