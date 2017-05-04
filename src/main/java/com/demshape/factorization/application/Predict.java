package com.demshape.factorization.application;

import com.demshape.factorization.datastructure.DatasetIO;
import com.demshape.factorization.tools.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jblas.FloatMatrix;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.FileReader;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class which reads in model embeddings and uses those to predict the most similar
 * items for a given list of ids based on the cosine similarity.
 */
public class Predict {

    private static final Logger logger = LogManager.getLogger(Predict.class);
    private static final int topK = 10;

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

            // ********************************************************************************
            // read embeddings
            logger.info("Reading embeddings...");
            Map<Integer, FloatMatrix> itemFeatureVectors = DatasetIO.readLatentVectors(new FileReader(options.output + "item_feature_embeddings.tsv"));

            // ********************************************************************************
            // predict top x most similar items
            Arrays.stream(options.similarItems).forEach(
                    item -> {
                        FloatMatrix itemVector = itemFeatureVectors.get(item);
                        if (itemVector != null) {
                            itemFeatureVectors.entrySet().stream().
                                    map(e -> new Candidate(e.getKey(), cosine(itemVector, e.getValue()))).
                                    sorted((a, b) -> -Double.compare(a.score, b.score)).
                                    limit(topK).
                                    map(a -> Integer.toString(a.id)).
                                    reduce((a, b) -> a + "," + b).
                                    ifPresent(r -> logger.info(String.format("Similar items for %d: %s", item, r)));
                        }
                    }
            );

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
     * Tuple class to store candidates similar to an item.
     */
    public static class Candidate {
        public int id;
        public double score;

        public Candidate(int id, double score) {
            this.id = id;
            this.score = score;
        }

        public String toString() {
            return String.format("%d -> %.4f", id, score);
        }
    }

    /**
     * Computes the cosine similarity between two vectors.
     * @param a first input vector
     * @param b second input vector
     * @return cosine similarity metric between a and b
     */
    public static double cosine(FloatMatrix a, FloatMatrix b) {
        double normA = Math.pow(a.norm2(), 2);
        double normB = Math.pow(b.norm2(), 2);
        return a.dot(b) / (Math.sqrt(normA) * Math.sqrt(normB));
    }

}
