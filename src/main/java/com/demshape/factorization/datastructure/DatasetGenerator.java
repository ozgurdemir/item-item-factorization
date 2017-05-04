package com.demshape.factorization.datastructure;

import org.jblas.FloatMatrix;

import java.util.*;

/**
 * Various functions to generate data sets.
 */
public class DatasetGenerator {

    /**
     * Split a data set into two data sets based on a given ratio.
     * @param dataset Input data set
     * @param ratio Ratio to split. 0.5 = 50%
     * @return A list of two data sets.
     */
    public static ArrayList<List<DataPoint>> split(List<DataPoint> dataset, double ratio) {
        List<DataPoint> splitted1 = new ArrayList<DataPoint>();
        List<DataPoint> splitted2 = new ArrayList<DataPoint>();
        int numSplitted = (int) Math.round(ratio * dataset.size());
        ArrayList<DataPoint> completeList = new ArrayList<DataPoint>(dataset);
        Collections.shuffle(completeList);
        for (int i = 0; i < dataset.size(); ++i) {
            if (i < numSplitted)
                splitted1.add(completeList.get(i));
            else
                splitted2.add(completeList.get(i));
        }
        ArrayList<List<DataPoint>> result = new ArrayList<List<DataPoint>>(2);
        result.add(splitted1);
        result.add(splitted2);
        return result;
    }

    public static ArrayList<List<DataPoint>> splitWithAll(List<DataPoint> dataset, double ratio) {
        List<DataPoint> splitted1 = new ArrayList<DataPoint>();
        List<DataPoint> splitted2 = new ArrayList<DataPoint>();
        Set<Integer> contextSet = new HashSet<Integer>();
        Set<Integer> itemSet = new HashSet<Integer>();
        int numSplitted = (int) Math.round((1.0 - ratio) * dataset.size());
        ArrayList<DataPoint> completeList = new ArrayList<DataPoint>(dataset);
        Collections.shuffle(completeList);
        for (DataPoint dataPoint : completeList) {
            if (!itemSet.contains(dataPoint.itemId) || !contextSet.contains(dataPoint.contextId) || splitted2.size() >= numSplitted) {
                contextSet.add(dataPoint.contextId);
                itemSet.add(dataPoint.itemId);
                splitted1.add(dataPoint);
            } else
                splitted2.add(dataPoint);
        }
        ArrayList<List<DataPoint>> result = new ArrayList<List<DataPoint>>(2);
        result.add(splitted1);
        result.add(splitted2);
        return result;
    }

    /**
     * Sums up two latent vector mappings. The factorization machines model will return a latent vector for each
     * additional item feature. This function is used to sum all of them in order to get a single embedding for each
     * item.
     * @param latentA A mapping from item id to a latent vector
     * @param latentB A mapping from item id to a latent vector
     * @return A mapping where corresponding item latent vectors have been summed up.
     */
    public static Map<Integer, FloatMatrix> sumLatentVectors(Map<Integer, FloatMatrix> latentA, Map<Integer, FloatMatrix> latentB) {
        Map<Integer, FloatMatrix> result = new HashMap<>();
        latentA.forEach((keyA, vectorA) -> {
                    FloatMatrix vectorB = latentB.get(keyA);
                    if (vectorB != null)
                        result.put(keyA, vectorA.add(vectorB));
                }
        );
        return result;
    }

}
