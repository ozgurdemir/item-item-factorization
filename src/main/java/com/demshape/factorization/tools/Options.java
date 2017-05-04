package com.demshape.factorization.tools;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;

/**
 * <a href="http://args4j.kohsuke.org/">Args4j</a> java bean used for all configuration.
 */
public class Options {
    @Option(name = "-dataset", usage = "path to a data set to split into train and test set", metaVar = "<string>")
    public String dataset;

    @Option(name = "-trainset", usage = "path to train set", metaVar = "<string>")
    public String trainset;

    @Option(name = "-testset", usage = "path to test set", metaVar = "<string>")
    public String testset;

    @Option(name = "-features", usage = "path to optional features in sparse format 'id column:value column:value...'", metaVar = "<string>")
    public String features;

    @Option(name = "-output", usage = "path to output folder", metaVar = "<string>")
    public String output;

    @Option(name = "-dimensions", usage = "number of features used for the model", metaVar = "<int>")
    public int dimensions;

    @Option(name = "-lambda1", usage = "regularization factor for the bias", metaVar = "<float>")
    public float lambda1;

    @Option(name = "-lambda2", usage = "regularization factor for the latent features", metaVar = "<float>")
    public float lambda2;

    @Option(name = "-splitratio", usage = "ratio to split data set into train and test set", metaVar = "<double>")
    public double splitratio = 1.0;

    @Option(name = "-iterations", usage = "maximum number of iterations for learning", metaVar = "<int>")
    public int iterations;

    @Option(name = "-alpha", usage = "smoothing for negative sampling", metaVar = "<int>")
    public double alpha = 0.75;

    @Option(name = "-learnrate", usage = "initial step size", metaVar = "<int>")
    public float learnRate;

    @Option(name = "-mincount", usage = "min number of co-occurrences used for weighting in regression", metaVar = "<int>")
    public int minCount = 100;

    @Option(name = "-threads", usage = "number of threads", metaVar = "<int>")
    public int threads;

    @Option(name = "-negatives", usage = "number of negative samples", metaVar = "<int>")
    public int negatives;

    @Option(name = "-lossfunction", usage = "logistic:logistic loss, hinge:smooth hinge loss, mse:mean squared error loss", metaVar = "<String>")
    public String lossFunction = "logistic";

    @Option(name = "-regression", handler = ExplicitBooleanOptionHandler.class, usage = "use a regression instead of a classification (see doc)", metaVar = "<bool>")
    public boolean regression = false;

    @Option(name = "-stream", handler = ExplicitBooleanOptionHandler.class, usage = "stream train set from disk instead of reading all into memory (use for large data sets)", metaVar = "<bool>")
    public boolean stream = false;

    @Option(name = "-help", usage = "print help information", metaVar = "<boolean>")
    public boolean help;

    @Option(name = "-rowbased", handler = ExplicitBooleanOptionHandler.class, usage = "", metaVar = "<boolean>")
    public boolean rowBased = false;

    @Option(name = "-window", usage = "window size for item item co interaction", metaVar = "<int>")
    public int window = 5;

    @Option(name = "-bins", usage = "bins used for evaluation")
    public int[] bins = new int[]{10, 20, 50, 100, 1000, 5000, 100000};

    @Option(name = "-similaritems", usage = "predict most similar items to these ones", metaVar = "<int>")
    public int[] similarItems = new int[]{};

    @Option(name = "-tablesize", usage = "Array size used for negative sampling table")
    public int tableSize = 100_000_000;

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
