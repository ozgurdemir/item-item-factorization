package com.demshape.factorization.model;

import com.demshape.factorization.dataset.DatasetInterface;
import com.demshape.factorization.datastructure.DataPoint;
import com.demshape.factorization.datastructure.SparseVector;
import com.demshape.factorization.lossfunction.LossFunctionInterface;
import com.demshape.factorization.metric.LossAverager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jblas.FloatMatrix;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import static com.demshape.factorization.tools.GlobalConstants.HASH_MAP_LOAD_FACTOR;
import static com.demshape.factorization.tools.GlobalConstants.hashSize;

public class Factorization {

    private static final Logger logger = LogManager.getLogger(Factorization.class);

    /**
     * number of features
     */
    public int dimensions;

    /**
     * initial learn rate for sgd
     */
    public float learnRate;

    /**
     * regularization factor for the bias
     */
    public float lambda1;

    /**
     * regularization factor for the latent features
     */
    public float lambda2;

    /**
     * number of learn iterations
     */
    public int iterations;

    /**
     * number of negative samples
     */
    public int negatives;

    /**
     * loss function to be used for training
     */
    public LossFunctionInterface lossFunction;

    /**
     * wheather a regreassion should be used instead of a classification
     */
    public boolean regression;

    /**
     * table size used for negative sampling
     */
    public int tableSize;

    /**
     * minimum count after which no weighting is applied to individual samples
     */
    public int minCount;

    /**
     * smoothing parameter for negative sampling
     */
    public double alpha;

    /**
     * bins used to calculate loss
     */
    public int[] bins;

    // internal data structures
    public FloatMatrix biases;
    public FloatMatrix[] latentVectors;
    public float[] gradientLengths;
    public int[] samplingTable;
    public int[] itemCount;
    public int[] contextCount;

    public Factorization() {
    }

    public Factorization(int dimensions, float learnRate, float lambda1, float lambda2, int iterations, int negatives, LossFunctionInterface lossFunction, boolean regression, int tableSize, int minCount, double alpha, int[] bins) {
        this.dimensions = dimensions;
        this.learnRate = learnRate;
        this.lambda1 = lambda1;
        this.lambda2 = lambda2;
        this.iterations = iterations;
        this.negatives = negatives;
        this.lossFunction = lossFunction;
        this.regression = regression;
        this.tableSize = tableSize;
        this.minCount = minCount;
        this.alpha = alpha;
        this.bins = bins;
    }

    public void train(DatasetInterface trainset, SparseVector[] optionalFeatures, int numFeatures) throws InterruptedException, ExecutionException {
        initParameters(numFeatures, dimensions);
        initLearnRates(numFeatures);
        count(numFeatures, trainset);
        if (negatives > 0)
            initSamplingTable(contextCount, alpha, tableSize);

        for (int iteration = 1; iteration <= iterations; ++iteration) {
            logger.info(String.format("Iteration %d start...", iteration));
            trainset.parallelStream().map(dataPoint -> {
                LossAverager lossAverager = new LossAverager(bins);
                final int itemOccurrence = itemCount[dataPoint.itemId];

                // positive data points
                float measured = 1.0f;
                float weight = dataPoint.rating;
                if (regression) {
                    measured = (float) Math.log(1.0 + dataPoint.rating);
                    weight = weight(dataPoint.rating);
                }
                SparseVector sparseVector = dataPoint.toSparseVector(optionalFeatures[dataPoint.itemId]);
                float loss = trainStep(sparseVector, measured, weight);
                lossAverager.add(loss, itemOccurrence);

                // negative sampling
                int numSamples = negatives;
                for (int i = 0; i < negatives; i++) {
                    int negativeContext = sample();
                    if (dataPoint.contextId == negativeContext) {
                        --numSamples;
                        continue;
                    }
                    dataPoint.contextId = negativeContext;
                    measured = -1.0f;
                    weight = dataPoint.rating / numSamples;
                    if (regression) {
                        measured = 0.0f; // log(1.0 + 0)
                        weight = weight(1.0f);
                    }
                    sparseVector = dataPoint.toSparseVector(optionalFeatures[dataPoint.itemId]);
                    loss = trainStep(sparseVector, measured, weight);
                    lossAverager.add(loss, itemOccurrence);
                }

                return lossAverager;
            }).reduce((a, b) -> a.add(b)).ifPresent(iterationLoss ->
                    logger.info("train loss: " + iterationLoss));
        }
    }

    public float trainStep(SparseVector sparseVector, float measured, float weight) {
        final float predicted = predict(sparseVector);
        final float error = weight * lossFunction.gDeriv(predicted, measured);

        // pre compute sum of vectors to speed up computation
        final FloatMatrix vectorSum = FloatMatrix.zeros(latentVectors[0].length);
        for (SparseVector.SparseEntry entry : sparseVector)
            vectorSum.addi(latentVectors[entry.key].mul(entry.value));

        // gradient updates
        for (SparseVector.SparseEntry entryA : sparseVector) {
            int key = entryA.key;
            float value = entryA.value;
            FloatMatrix vector = latentVectors[key];

            // quadratic term
            final FloatMatrix gradientA = vectorSum.mul(value).subi(vector.mul(value * value));
            gradientA.muli(error).addi(vector.mul(lambda2));
            float individualLearnRate = updateLearnRate(key, gradientA);
            vector.subi(gradientA.muli(learnRate * individualLearnRate));

            // linear term
            float current = biases.get(key);
            float linearGradient = value * error + lambda1 * current;
            biases.put(key, current - learnRate * individualLearnRate * linearGradient);
        }

        // note that reported loss does not contain regularization term
        return weight * lossFunction.g(predicted, measured);
    }

    public float predict(SparseVector sparseVector) {
        float predicted = 0.0f;
        for (int i = 0; i < sparseVector.size(); ++i) {
            // linear term
            SparseVector.SparseEntry entryA = sparseVector.get(i);
            int keyA = entryA.key;
            float valueA = entryA.value;
            FloatMatrix vectorA = latentVectors[keyA];
            predicted += valueA * biases.get(keyA);

            // quadratic term
            // XXX(od): do not include quadratic features
            for (int j = i + 1; j < sparseVector.size(); ++j) {
                SparseVector.SparseEntry entryB = sparseVector.get(j);
                int keyB = entryB.key;
                float valueB = entryB.value;
                FloatMatrix vectorB = latentVectors[keyB];
                predicted += valueA * valueB * vectorA.dot(vectorB);
            }
        }
        return predicted;
    }

    /**
     * Predict loss on a data set e.g. test set
     * @param dataset Input data set
     * @param optionalFeatures optional features of the data set
     * @return Binned losses
     */
    public LossAverager predict(DatasetInterface dataset, SparseVector[] optionalFeatures) {
        return dataset.parallelStream().map(dataPoint -> {
            LossAverager lossAverager = new LossAverager(bins);
            SparseVector sparseVector = dataPoint.toSparseVector(optionalFeatures[dataPoint.itemId]);
            float predicted = predict(sparseVector);
            int itemOccurrence = itemCount[dataPoint.itemId];

            float measured = 1.0f;
            float weight = dataPoint.rating;
            if (regression) {
                measured = (float) Math.log(1.0f + dataPoint.rating);
                weight = weight(dataPoint.rating);
            }

            float loss = weight * lossFunction.g(predicted, measured);
            lossAverager.add(loss, itemOccurrence);

            // negative samples
            int numSamples = negatives;
            for (int i = 0; i < negatives; i++) {
                int negativeContext = sample();
                if (dataPoint.contextId == negativeContext) {
                    --numSamples;
                    continue;
                }
                dataPoint.contextId = negativeContext;
                measured = -1.0f;
                weight = dataPoint.rating / numSamples;
                if (regression) {
                    measured = 0.0f; // log(1.0 + 0)
                    weight = weight(1.0f);
                }
                sparseVector = dataPoint.toSparseVector(optionalFeatures[dataPoint.itemId]);
                loss = weight * lossFunction.g(predict(sparseVector), measured);
                lossAverager.add(loss, itemOccurrence);
            }
            return lossAverager;
        }).reduce((a, b) -> a.add(b)).get();
    }

    /**
     * Updates sum of gradient length and returns updated learn rate
     * @param id id of item
     * @param gradient current gradient
     * @return updated learn rate
     */
    public float updateLearnRate(int id, FloatMatrix gradient) {
        gradientLengths[id] += gradientLength(gradient);
        return learnRate(gradientLengths[id]);
    }

    public float gradientLength(FloatMatrix gradient) {
        return gradient.dot(gradient) / (float) gradient.length;
    }

    /**
     * Returns AdaGrad learn rate
     * @param gradientLength current gradient length
     * @return AdaGrad learn rate
     */
    public float learnRate(float gradientLength) {
        return (float) (1.0 / Math.sqrt(gradientLength));
    }

    /**
     * Weight based on number of co-occurrences
     * @param score Score of pair
     * @return weight based on score
     */
    public float weight(float score) {
        if (score <= minCount)
            return (float) Math.pow(score / minCount, alpha);
        return 1.0f;
    }

    /**
     * Init context, item and feature latent vectors with a normal distribution and
     * set biases to 0.
     * @param numFeatures number of additional features
     * @param dimensions number of latent factors
     */
    public void initParameters(int numFeatures, int dimensions) {
        logger.info("initializing parameters...");
        biases = FloatMatrix.zeros(numFeatures);
        latentVectors = new FloatMatrix[numFeatures];
        for (int i = 0; i < numFeatures; ++i)
            latentVectors[i] = gaussVector(dimensions).divi(dimensions);
    }

    public void initLearnRates(int numFeatures) {
        logger.info("initializing learn rates...");
        gradientLengths = new float[numFeatures];
    }

    /**
     * Number of item occurrences in the train set. Used to build up the sample table and bin the results.
     * Note that the result array is not dense. Context and item indices will be
     * filled in with zeros.
     * @param numFeatures number of features
     * @param dataset the train set
     */
    public void count(int numFeatures, DatasetInterface dataset) {
        logger.info("counting item occurrence...");
        itemCount = new int[numFeatures];
        contextCount = new int[numFeatures];
        for (DataPoint dataPoint : dataset) {
            contextCount[dataPoint.contextId]++;
            itemCount[dataPoint.itemId]++;
        }
    }

    /**
     * Creates a sampling table for a given distribution.
     * Example for size = 3 and distribution = 1 to 10, 2 to 20 will result in int[] = {1, 2, 2}
     * Hence, a uniform sampling over this samplingTable will result in a distribution similar to the given
     * one.
     * @param itemCount an array which holds the item occurrences
     * @param alpha smoothing parameter
     * @param tableSize sampling table size
     */
    public void initSamplingTable(int[] itemCount, double alpha, int tableSize) {
        logger.info("initializing sampling table...");
        samplingTable = new int[tableSize];
        double sum = 0.0;
        for (int itemId = 0; itemId < itemCount.length; ++itemId)
            sum += Math.pow(itemCount[itemId], alpha);

        int tableIndex = 0;
        double cumulative = 0.0;
        for (int itemId = 0; itemId < itemCount.length; ++itemId) {
            if (itemCount[itemId] > 0) {
                cumulative += Math.pow(itemCount[itemId], alpha) / sum;
                while (cumulative > (tableIndex / (double) tableSize) && tableIndex < tableSize)
                    samplingTable[tableIndex++] = itemId;
            }
        }
    }

    /**
     * Samples a negative item according to samplingTable distribution
     * @return id of sampled item
     */
    public int sample() {
        return samplingTable[ThreadLocalRandom.current().nextInt(tableSize)];
    }

    /**
     * Returns a vector initialized with a gaussian distribution
     * mean = 1, std = 1
     * @param dimensions number of dimensions
     * @return Normalized vector
     */
    public FloatMatrix gaussVector(int dimensions) {
        return FloatMatrix.randn(dimensions);
    }

    /**
     * Given a dense mapping returns feature latent vectors
     * @param map mapping from initial to dense ids
     * @return mapping from original id to latent vectors
     */
    public Map<Integer, FloatMatrix> vectors(Map<Integer, Integer> map) {
        Map<Integer, FloatMatrix> vectors = new HashMap<>(hashSize(map.size()), HASH_MAP_LOAD_FACTOR);
        map.forEach((id, mappedId) -> vectors.put(id, vector(mappedId)));
        return vectors;
    }

    public FloatMatrix vector(int mappedId) {
        return latentVectors[mappedId];
    }

    /**
     * Returns context / item latent vectors summed up with feature latent vectors
     * @param map mapping from original to dense ids
     * @param featureMap embeddings of additional features
     * @param unmappedFeatures unmapped features
     * @return context / item latent vectors summed up with feature latent vectors
     */
    public Map<Integer, FloatMatrix> featureVectors(Map<Integer, Integer> map, Map<Integer, Integer> featureMap, Map<Integer, SparseVector> unmappedFeatures) {
        Map<Integer, FloatMatrix> vectors = new HashMap<>(hashSize(map.size()), HASH_MAP_LOAD_FACTOR);
        map.forEach((id, mappedId) -> {
            FloatMatrix summedVector = vector(mappedId).dup();
            unmappedFeatures.getOrDefault(id, new SparseVector(0)).forEach((unmappedFeature) ->
                    summedVector.addi(vector(featureMap.get(unmappedFeature.key)).mul(unmappedFeature.value)));
            vectors.put(id, summedVector);
        });
        return vectors;
    }

    /**
     * Returns embeddings of additional features
     * @param featureMap embeddings of additional features
     * @param unmappedFeatures unmapped features
     * @return embeddings of additional features
     */
    public Map<Integer, FloatMatrix> featureVectors(Map<Integer, Integer> featureMap, Map<Integer, SparseVector> unmappedFeatures) {
        Map<Integer, FloatMatrix> vectors = new HashMap<>(hashSize(unmappedFeatures.size()), HASH_MAP_LOAD_FACTOR);
        unmappedFeatures.forEach((id, sparseVector) -> {
            FloatMatrix summedVector = FloatMatrix.zeros(dimensions);
            sparseVector.forEach((unmappedFeature) ->
                    summedVector.addi(vector(featureMap.get(unmappedFeature.key)).mul(unmappedFeature.value)));
            vectors.put(id, summedVector);
        });
        return vectors;
    }

}
