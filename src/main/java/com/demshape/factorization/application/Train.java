package com.demshape.factorization.application;

import com.demshape.factorization.dataset.*;
import com.demshape.factorization.datastructure.DatasetIO;
import com.demshape.factorization.datastructure.SparseVector;
import com.demshape.factorization.lossfunction.LossFunctionFactory;
import com.demshape.factorization.model.Factorization;
import com.demshape.factorization.metric.LossAverager;
import com.demshape.factorization.tools.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Generates item embeddings from a train set and tests their performance on a test set. Context, item, feature vectors
 * and all their combinations are saved to disk.
 * <p>
 * options.trainset Path to the train set options.features Path to additional features of the train samples (optional)
 * options.rowBased Whether the train set is in line based format or not (see README.md) options.dimensions The number
 * of latent features options.learnRate The initial learn rate of the stochastic gradient descent procedure
 * options.lambda1 The regularization factor for the bias options.lambda2 The regularization factor for the latent
 * features options.iterations Total number of train iterations options.negatives Number of negative samples
 * options.lossFunction Loss function to be used (see {@link LossFunctionFactory}) options.regression Whether to use a
 * regression or a classification. options.tableSize The table size used for the distributed negative sampling.
 * options.minCount If item occurs less than minCount a weighting between 0 &lt; 1 is performed
 * (see {@link Factorization#weight(float)}).
 * options.alpha The smoothing parameter for negative sampling (see paper) options.bins
 * Array of int defining the bins used to calculate performance options.testset Prediction loss on test set (Optional)
 * options.output The root output folder. Several files will be generated within this folder.
 */
public class Train {

    private static final Logger logger = LogManager.getLogger(Train.class);
    private static final String MAPPED_SUFFIX = ".mapped";

    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        Options options = new Options();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            // ********************************************************************************
            // read parameters
            parser.parseArgument(args);
            if (options.help || args.length == 0)
                throw new CmdLineException(parser, new Throwable("Please set parameters"));
            logger.info(options);

            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", Integer.toString(options.threads));
            // ********************************************************************************
            // read optional features
            Map<Integer, SparseVector> unmappedFeatures = new HashMap<>();
            if (options.features != null) {
                logger.info("Reading optional features...");
                unmappedFeatures = DatasetIO.readFeatures(new FileReader(options.features));
                logger.info(String.format("read features for %d items", unmappedFeatures.size()));
            }

            // ********************************************************************************
            // map data set into dense representation
            logger.info("Creating dense data set...");
            DatasetInterface trainset;
            String mappedTrainsetPath = options.trainset + MAPPED_SUFFIX;
            DenseMapper denseMapper = new DenseMapper();
            if (options.rowBased) {
                int[][] lines = DatasetIO.readRowBased(options.trainset);
                denseMapper.learn(lines, unmappedFeatures);
                denseMapper.map(lines);
                trainset = new RowBasedDataset(lines, options.window, new Random().nextLong());
            } else {
                trainset = getDataset(options.trainset, options.stream);
                denseMapper.learn(trainset, unmappedFeatures);
                // write dense data set to disk
                denseMapper.write(trainset, new FileWriter(mappedTrainsetPath));
                trainset = getDataset(mappedTrainsetPath, options.stream);
            }
            SparseVector[] features = denseMapper.map(unmappedFeatures);

            // ********************************************************************************
            // build model
            Factorization factorization = new Factorization(
                    options.dimensions,
                    options.learnRate,
                    options.lambda1,
                    options.lambda2,
                    options.iterations,
                    options.negatives,
                    LossFunctionFactory.get(options.lossFunction),
                    options.regression,
                    options.tableSize,
                    options.minCount,
                    options.alpha,
                    options.bins
            );
            factorization.train(trainset, features, denseMapper.numFeatures);

            // ********************************************************************************
            // predict performance on test set
            if (options.testset != null) {
                logger.info("Reading test set");
                int testsetSize = DatasetIO.numberOfLines(new FileReader(options.testset));
                if (testsetSize > 0) {
                    DatasetInterface unmappedTestset = new DiskBasedDataset(options.testset, testsetSize);
                    DatasetInterface testset = denseMapper.map(unmappedTestset);
                    final double percentage = 100.0 * testset.size() / (double) unmappedTestset.size();
                    logger.info(String.format("%d out of %d (%.2f%%) data points predictable from test set", testset.size(), testsetSize, percentage));
                    LossAverager lossAverager = factorization.predict(testset, features);
                    logger.info(lossAverager);
                }
            }
            // ********************************************************************************
            // write latent factors to file
            logger.info("Writing biases to file...");
            DatasetIO.write(factorization.biases, new FileWriter(options.output + "biases.tsv"));
            logger.info("Writing latent features to file...");
            DatasetIO.write(factorization.vectors(denseMapper.contextMap), new FileWriter(options.output + "context_embeddings.tsv"));
            DatasetIO.write(factorization.vectors(denseMapper.itemMap), new FileWriter(options.output + "item_embeddings.tsv"));
            DatasetIO.write(factorization.featureVectors(denseMapper.featureMap, unmappedFeatures), new FileWriter(options.output + "feature_embeddings.tsv"));
            DatasetIO.write(factorization.featureVectors(denseMapper.contextMap, denseMapper.featureMap, unmappedFeatures), new FileWriter(options.output + "context_feature_embeddings.tsv"));
            DatasetIO.write(factorization.featureVectors(denseMapper.itemMap, denseMapper.featureMap, unmappedFeatures), new FileWriter(options.output + "item_feature_embeddings.tsv"));

        } catch (CmdLineException e) {
            parser.printUsage(System.out);
            logger.error("Please check your parameters:", e);
        } catch (Exception e) {
            logger.error("Error:", e);
        } finally {
            logger.info("Program finished.");
        }
    }

    /**
     * Reads in a data set from a given path. If stream is true the data set will be streamed entry by entry instead
     * of reading it into memory at once. Reading into memory enables faster repeated processing during training.
     * Streaming on the other hand makes it possible to use large data sets which do not fit into main memory.
     *
     * @param path   Path to the data set
     * @param stream Whether to stream from disk or read in the whole file into main memory
     * @return A data set
     * @throws IOException if path can not be read
     */
    public static DatasetInterface getDataset(String path, boolean stream) throws IOException {
        logger.info("Counting data set lines");
        int numLines = DatasetIO.numberOfLines(new FileReader(path));
        if (stream) {
            logger.info("Streaming train set from disk");
            return new DiskBasedDataset(path, numLines);
        } else {
            logger.info("Reading train set");
            DatasetInterface dataset = new LowMemoryDataset(numLines);
            DatasetIO.readColumnBased(path, dataset);
            logger.info(String.format("Finished reading train set: %d data points detected.", dataset.size()));
            return dataset;
        }
    }

}
