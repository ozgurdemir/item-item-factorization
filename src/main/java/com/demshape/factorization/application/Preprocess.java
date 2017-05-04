package com.demshape.factorization.application;

import com.demshape.factorization.datastructure.DatasetIO;
import com.demshape.factorization.tools.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.Locale;

/**
 * Utility class which reads in a data set, shuffles it and splits it into a train and test set.
 * The shuffled data set will be saved under options.dataset + "-shuffled". The train and test sets will be saved under
 * options.trainset and options.testset respectively.
 */
public class Preprocess {

    private static final Logger logger = LogManager.getLogger(Preprocess.class);

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
            // shuffle
            logger.info("Shuffling dataset");
            int bufferSize = 10000000;
            DatasetIO.shuffle(new FileReader(options.dataset),
                    new FileWriter(options.dataset + "-shuffled"), bufferSize);

            // ********************************************************************************
            // split into train and testset
            if (options.splitratio < 1.0) {
                logger.info("Splitting data set into train and test set...");
                DatasetIO.splitToFile(new FileReader(options.dataset + "-shuffled"),
                        new FileWriter(options.trainset), new FileWriter(options.testset), options.splitratio);
            }

        } catch (CmdLineException e) {
            parser.printUsage(System.out);
            logger.error("Please check your parameters:", e);
        } catch (Exception e) {
            logger.error("Error:", e);
        } finally {
            logger.info("Program finished.");
        }
    }
}
