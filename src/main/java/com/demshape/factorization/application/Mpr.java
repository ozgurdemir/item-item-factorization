package com.demshape.factorization.application;

import com.demshape.factorization.datastructure.DatasetIO;
import com.demshape.factorization.tools.Options;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jblas.FloatMatrix;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Utility class which computes the mpr for a given train set.
 */
public class Mpr {

    private static final Logger logger = LogManager.getLogger(Mpr.class);

    private static int numSamples = 50;

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
            // read embeddings
            logger.info("Reading embeddings...");
            Map<Integer, FloatMatrix> itemFeatureVectors = DatasetIO.readLatentVectors(new FileReader(options.testset));
            logger.info(String.format("Found %d embeddings", itemFeatureVectors.size()));

            int[] catalogue = ArrayUtils.toPrimitive(itemFeatureVectors.keySet().toArray(new Integer[0]));
            // ********************************************************************************
            // read test set
            List<MprEntry> mprEntryList = read(new FileReader(options.output + "/mpr.tsv"));
            logger.info(String.format("Read %d mpr entries", mprEntryList.size()));

            // compute mpr using embeddings
            logger.info("Predicting mpr using embeddings");
            List<MprEntry> mprPredictionList = mprEntryList.stream().parallel().map(mprEntry -> {
                FloatMatrix seedVector = itemFeatureVectors.get(mprEntry.seedId);
                FloatMatrix interactionVector = itemFeatureVectors.get(mprEntry.interactionId);

                mprEntry.score = -1;
                if (seedVector != null && interactionVector != null) {
                    final double posCosine = cosine(seedVector, interactionVector);
                    int sampleIsBetter = 0;
                    for (int i = 0; i < numSamples; ++i) {
                        FloatMatrix negativeVector = itemFeatureVectors.get(sample(catalogue));
                        final double negCosine = cosine(seedVector, negativeVector);
                        if (negCosine > posCosine)
                            ++sampleIsBetter;
                    }
                    mprEntry.score = sampleIsBetter / (double) numSamples;
                }
                return mprEntry;
            }).filter(m -> m.score >= 0).collect(Collectors.toList());

            double percentage = 100.0 * mprPredictionList.size() / (double) mprEntryList.size();
            logger.info(String.format("%d out of %d (%.2f%%) predictable", mprPredictionList.size(), mprEntryList.size(), percentage));
            List<MprEntry> mprList = mprPredictionList;

            // compute overall average
            double average = mprList.stream().mapToDouble(m -> m.score).average().getAsDouble();
            logger.info(String.format("Global mpr is: %.4f", average));

            // compute binned average
            Map<Integer, List<MprEntry>> grouped = mprList.stream().collect(Collectors.groupingBy(m -> m.bin));
            Map<Integer, List<MprEntry>> sorted = new TreeMap<>(grouped);
            sorted.forEach((bin, list) -> {
                double binMpr = list.stream().mapToDouble(m -> m.score).average().getAsDouble();
                logger.info(String.format("Bin: %d mpr is: %.4f", bin, binMpr));
            });

        } catch (CmdLineException e) {
            parser.printUsage(System.out);
            logger.error("Please check your parameters:", e);
        } catch (Exception e) {
            logger.error("Error:", e);
        } finally {
            logger.info("Program finished.");
        }
    }

    private static int sample(int[] catalogue) {
        return catalogue[ThreadLocalRandom.current().nextInt(catalogue.length)];
    }

    private static List<MprEntry> read(Reader reader) throws IOException {
        Splitter splitter = Splitter.on('\t');
        List<MprEntry> result = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            Iterator<String> i = splitter.split(line).iterator();
            MprEntry mprEntry = new MprEntry();
            mprEntry.seedId = Integer.parseInt(i.next());
            mprEntry.interactionId = Integer.parseInt(i.next());
            mprEntry.bin = Integer.parseInt(i.next());
            mprEntry.score = Double.parseDouble(i.next());
            result.add(mprEntry);
        }
        return result;
    }

    public static class MprEntry {
        int seedId;
        int interactionId;
        int bin;
        double score;
    }

    public static double cosine(FloatMatrix a, FloatMatrix b) {
        double normA = Math.pow(a.norm2(), 2);
        double normB = Math.pow(b.norm2(), 2);
        return a.dot(b) / (Math.sqrt(normA) * Math.sqrt(normB));
    }

}
