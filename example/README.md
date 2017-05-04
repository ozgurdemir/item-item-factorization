# Example data set
This folder contains an example toy data set (dataset-row.tsv). Each row of the data set contains interactions of a single user:

    user_a interaction_1 interaction_2 interaction_3...
    user_b interaction_1 interaction_2 interaction_3...
    ...

An alternative input format consists of three columns (dataset-column.tsv):

    item_a item_b score
    item_a item_c score

where score might denote the times two items have been interacted with together.

In this toy data set items with a single digit id, are interacted with together more often than items with two digits. In other words items with a single digit id are more similar to each other than those with two digits.

# Preprocess
Let's randomly shuffle and split our data set into a train and test set

    > make preprocess OPTIONS=example/options.txt

This step will generate three new files.
- dataset-row.tsv-shuffled: the data set where the rows have been randomly shuffled
- trainset.tsv: ~70% of the data set for training
- testset.tsv: ~30% of the data set for testing

# Train
Let's train the model using the newly created train and test splits.

    > make train OPTIONS=example/options.txt

This step will read in the train set and additional features per item (features.txt) and produce embeddings (XXX_embeddings.tsv) for each item.

# Predict

Finally we'll predict the test set using the trained embeddings

    > make predict OPTIONS=example/options.txt

This will display the most similar items for a given list of example items:

    > Similar items for 1: 1,4,6,3,5,2,10,50,40,60

Have a look at the [options.txt](options.txt) file which contains all relevant options for the above tasks.
