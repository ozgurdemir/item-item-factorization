package com.demshape.factorization.dataset;

import com.demshape.factorization.datastructure.DataPoint;
import com.demshape.factorization.datastructure.SparseVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.demshape.factorization.tools.GlobalConstants.HASH_MAP_LOAD_FACTOR;

/**
 * Maps a data set into a dense one: all input ids are remapped from 0 to max(id).
 * A dense representation allows to allocate arrays instead of maps and speeds up subsequent computations.
 * The mapping is stored internally such that the remapping can be reversed.
 */
public class DenseMapper {
    private static final Logger logger = LogManager.getLogger(DenseMapper.class);

    /**
     * The set of all context ids
     */
    public Set<Integer> contexts;

    /**
     * The set of all items
     */
    public Set<Integer> items;

    /**
     * The set of all features
     */
    public Set<Integer> features;

    /**
     * The mapping from context id to the dense one.
     */
    public Map<Integer, Integer> contextMap;

    /**
     * The mapping from item id to the dense one.
     */
    public Map<Integer, Integer> itemMap;

    /**
     * The mapping from feature id to the dense one.
     */
    public Map<Integer, Integer> featureMap;
    public int numFeatures;

    /**
     * Detects all elements in a column based data set and creates mappings from original ids to dense ones.
     * @param dataset An input dataset
     * @param optionalFeatures Optional side information available for each item.
     */
    public void learn(DatasetInterface dataset, Map<Integer, SparseVector> optionalFeatures) {
        detect(dataset);
        detect(optionalFeatures);
        numFeatures = createMap(contexts, items, features);
        logger.info(String.format("%d contexts, %d items, %d features found (total: %d)", contexts.size(), items.size(), features.size(), numFeatures));
    }

    /**
     * Detects all elements in row based based data set and creates mappings from original ids to dense ones.
     * @param lines An input dataset
     * @param optionalFeatures Optional side information available for each item.
     */
    public void learn(int[][] lines, Map<Integer, SparseVector> optionalFeatures) {
        detect(lines);
        detect(optionalFeatures);
        numFeatures = createMap(contexts, items, features);
        logger.info(String.format("%d contexts, %d items, %d features found (total: %d)", contexts.size(), items.size(), features.size(), numFeatures));
    }

    /**
     * Detects all contexts and items of a column based data set. This is done concurrently.
     * @param dataset Input data set
     */
    public void detect(DatasetInterface dataset) {
        logger.info("Detecting contexts and items");
        contexts = Collections.newSetFromMap(new ConcurrentHashMap<>());
        items = Collections.newSetFromMap(new ConcurrentHashMap<>());
        dataset.parallelStream().forEach(
                dataPoint -> {
                    contexts.add(dataPoint.contextId);
                    items.add(dataPoint.itemId);
                }
        );
    }

    /**
     * Detects all contexts and items of a row based data set. This is done concurrently.
     * @param lines Input data set
     */
    public void detect(int[][] lines) {
        logger.info("Detecting contexts and items");
        contexts = Collections.newSetFromMap(new ConcurrentHashMap<>());
        items = Collections.newSetFromMap(new ConcurrentHashMap<>());
        Arrays.stream(lines).parallel().forEach(currentLine -> {
            for (int item = 0; item < currentLine.length; ++item) {
                contexts.add(currentLine[item]);
                items.add(currentLine[item]);
            }
        });
    }

    /**
     * Detects all additional features.
     * @param itemFeatures A mapping from item id to its sparse vector of additional features
     */
    public void detect(Map<Integer, SparseVector> itemFeatures) {
        logger.info("Detecting additional features");
        features = new HashSet<>();
        for (SparseVector sparseVector : itemFeatures.values()) {
            for (SparseVector.SparseEntry sparseEntry : sparseVector)
                features.add(sparseEntry.key);
        }
    }

    /**
     * Generates a dense mapping given all contexts, items and additional features
     * The context ids have odd indices whereas the item ids have even ones. That way the context vectors
     * for item i can be accessed via i + 1.
     * @param contexts The set of all context ids
     * @param items The set of all item ids
     * @param features The set of all feature ids
     * @return Number of total elements
     */
    public int createMap(Set<Integer> contexts, Set<Integer> items, Set<Integer> features) {
        logger.info("Creating dense mapping");
        contextMap = new HashMap<>((int) (contexts.size() / HASH_MAP_LOAD_FACTOR), HASH_MAP_LOAD_FACTOR);
        itemMap = new HashMap<>((int) (items.size() / HASH_MAP_LOAD_FACTOR), HASH_MAP_LOAD_FACTOR);
        featureMap = new HashMap<>((int) (features.size() / HASH_MAP_LOAD_FACTOR), HASH_MAP_LOAD_FACTOR);
        int index = 0;
        for (int id : items) {
            itemMap.put(id, 2 * index);
            contextMap.put(id, 1 + 2 * index);
            index++;
        }
        index = 2 * index;
        for (int id : features)
            featureMap.put(id, index++);
        return index;
    }

    /**
     * Return a dense data set using the learned mapping.
     * @param dataset A non dense column based data set
     * @return A dense data set
     */
    public DatasetInterface map(DatasetInterface dataset) {
        DatasetInterface mappedDateset = new LowMemoryDataset((int) dataset.size());
        for (DataPoint dataPoint : dataset) {
            Integer contextId = contextMap.get(dataPoint.contextId);
            Integer itemId = itemMap.get(dataPoint.itemId);
            if (contextId != null && itemId != null) {
                dataPoint.contextId = contextId;
                dataPoint.itemId = itemId;
                mappedDateset.addDatapoint(dataPoint);
            }
        }
        return mappedDateset;
    }

    /**
     * Return a dense data set using the learned mapping.
     * @param lines A non dense line based data set
     */
    public void map(int[][] lines) {
        logger.info("Mapping line based data set to dense representation");
        Arrays.stream(lines).parallel().forEach(currentLine -> {
            for (int item = 0; item < currentLine.length; ++item)
                currentLine[item] = itemMap.get(currentLine[item]);
        });
    }

    /**
     * @param optionalFeatures A mapping from item id to sparse additional features.
     * @return A dense sparse feature representation. Both item ids and vector keys are mapped.
     */
    public SparseVector[] map(Map<Integer, SparseVector> optionalFeatures) {
        SparseVector[] mappedFeatures = new SparseVector[numFeatures];
        optionalFeatures.forEach((itemId, sparseVector) -> {
            SparseVector mappedVector = new SparseVector(sparseVector.size());
            int i = 0;
            for (SparseVector.SparseEntry entry : sparseVector)
                mappedVector.set(i++, featureMap.get(entry.key), entry.value);
            if (itemMap.containsKey(itemId))
                mappedFeatures[itemMap.get(itemId)] = mappedVector;
        });
        return mappedFeatures;
    }

    /**
     * Writes a dense mapped data set to disk.
     * @param dataset Input data set
     * @param writer Output writer
     * @throws IOException if writer is not writable
     */
    public void write(DatasetInterface dataset, Writer writer) throws IOException {
        logger.info("Writing dense mapped data set");
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(writer), false);
        dataset.parallelStream().forEach(dataPoint -> {
            dataPoint.contextId = contextMap.get(dataPoint.contextId);
            dataPoint.itemId = itemMap.get(dataPoint.itemId);
            printWriter.println(dataPoint.toString());
        });
        printWriter.close();
    }
}
