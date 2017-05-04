[![Build Status](https://travis-ci.org/soundcloud/item-item-factorization.svg?branch=master)](https://travis-ci.org/soundcloud/item-item-factorization)

# Item Item Factorization With Side Information
This is an implementation of factorization machines specialized to generate item embeddings.
See [item-item-factorization.pdf](http://www.arxiv.org) for an overview of the algorithm.
# Build
	> make build

This is a java maven project. Maven will be downloaded by 'make build'. If maven is allready available all maven commands can be called directly on the root folder e.g.

	> mvn package
	> mvn test
	> mvn compile

# How to Run
There are three entry points.
## 1. Preprocess.java
- Randomly shuffles a given data set
- Splits the shuffled data set into test and train set

## 2. Train.java
- Trains a factorization machines model on the train data.
- Predicts on optional test set
- Saves latent vectors to disk.

## 3. Predict.java
- Reads in latent vectors
- Finds most similar items to a given list of input items

# Starting
Above entry points can be started using the corresponding make directives e.g.

	> make preprocess
	> make train
	> make predict

or by calling the java classes directly

    > java -server -Xmx4g -Xms1g -cp com.demshape.factorization.application.Preprocess
    > java -server -Xmx4g -Xms1g -cp com.demshape.factorization.application.Train
    > java -server -Xmx4g -Xms1g -cp com.demshape.factorization.application.Predict

All entry points log their output to the console as well as to a log file (log.txt).

# Parameters
All parameters are set via a configuration file. See [options-example.txt](options-example.txt) for an example configuration file.

	> make preprocess OPTIONS=options.txt

Note that *options.txt* is the default file name used if *OPTIONS* parameter is not specified.

# List of Parameters

- **alpha** (int)           : smoothing for negative sampling (default: 0.75)
- **bins** ([]int)          : bins used for evaluation (default: 10,20,50,100,1000,5000,100000)
- **dataset** (string)      : path to a data set to split into train and test set
- **dimensions** (int)      : number of features used for the model (default: 0)
- **features** (string)     : path to optional features in sparse format 'id											 column:value column:value...'
- **help** (boolean)        : print help information (default: false)
- **iterations** (int)      : maximum number of iterations for learning (default: 0)
- **lambda1** (float)       : lambda regularization parameter for the model											 (default: 0.0)
- **lambda2** (float)       : lambda regularization parameter for the creator										 features (default: 0.0)
- **learnrate** (int)       : initial step size (default: 0.0)
- **lossfunction** (String) : logistic:logistic loss, hinge:smooth hinge loss,
												 mse:mean squared error loss (default: logistic)
- **mincount** (int)        : min number of co-occurrences used for weighting in
												 regression (default: 100)
- **negatives** (int)       : number of negative samples (default: 0)
- **output** (string)       : path to output folder
- **regression** (bool)     : use a regression instead of a classification (see
												 doc) (default: false)
- **similaritems** ([]int)  : predict most similar items to these ones
- **splitratio** (double)   : ratio to split data set into train and test set
												 (default: 1.0)
- **stream** (bool)         : stream train set from disk instead of reading all
												 into memory (use for large data sets) (default: false)
- **tablesize** (int)       : Array size used for negative sampling table (default:
												 100000000)
- **testset** (string)      : path to test set
- **threads** (int)         : number of threads (default: 0)
- **trainset** (string)     : path to train set
- **window** (int)          : window size for item item co interaction (default: 5)

# Input Format
All input files are tab separated text files.
## Train Set
The input format of the train set can be either row or column based.

### Row based format
In row based format, each row of the data set contains interactions of a single user:

    user_a interaction_1 interaction_2 interaction_3...
    user_b interaction_1 interaction_2 interaction_3...
    ...
Pairs of co-occurrences are generated on the fly while streaming through the data set.
The pairs are generated based on a given input window size.

### Column based format
The column based format consists of three columns. The first two columns contain a pair of items where the third
 column contains the score (similarity) between those two item:

    item_a item_b score
    item_a item_c score

This format allows to pre-compute the co-occurrences in a custom way.

## Additional side Information
The additional side information is in sparse vector format:

		ITEM_ID FEATURE_ID:FEATURE_VALUE FEATURE_ID:FEATURE_VALUE...

where FEATURE_ID is 1 based e.g.

		234 1:0.4 2:0.6 ...
		755 1:0.1 2:0.2 ...

# Example
The example folder contains a little toy example. See [README.md](example/README.md) for a description.

# Versioning
This library aims to adhere to Semantic Versioning 2.0.0. Violations of this scheme should be reported as bugs.
Specifically, if a minor or patch version is released that breaks backward compatibility, that version should be
immediately yanked and/or a new version should be immediately released that restores compatibility.
Breaking changes will only be introduced with new major versions.
As a result of this policy, you can (and should) specify a dependency on this gem using the Pessimistic Version
Constraint with two digits of precision.

# Copyright

Copyright (c) 2017 Soundcloud

See the [LICENSE](LICENSE.txt) file for details.
