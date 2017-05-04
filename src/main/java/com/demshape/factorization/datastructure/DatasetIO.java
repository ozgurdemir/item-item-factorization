package com.demshape.factorization.datastructure;

import com.demshape.factorization.dataset.DatasetInterface;
import com.demshape.factorization.dataset.RowBasedDataset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jblas.FloatMatrix;

import java.io.*;
import java.util.*;

/**
 * Class which holds all IO operations.
 */
public class DatasetIO {

    private static final Logger logger = LogManager.getLogger(DatasetIO.class);

    /**
     * Read a data set from a txt file in column based format.
     * 1st column: item a (int)
     * 2nd column: item b (int)
     * 3rd column: similarity between item a and item b e.g. number of co-occurrences (float)
     * @param filepath Path to input file
     * @param dataset A dataset instance to read into
     * @throws IOException if path is not readable
     */
    public static void readColumnBased(String filepath, DatasetInterface dataset) throws IOException {
        logger.info("Reading from file: " + filepath);
        BufferedReader bufReader = new BufferedReader(new FileReader(filepath));
        String line;
        int lineNumber = 0;
        while ((line = bufReader.readLine()) != null) {
            ++lineNumber;
            if (lineNumber % 10000000 == 0)
                logger.info(String.format("Reading line: %d", lineNumber));
            dataset.addDatapoint(new DataPoint(line));
        }
        bufReader.close();
        logger.info(String.format("read %d datapoints", lineNumber));
    }

    /**
     * Read a data set from a txt file in row based format.
     * Each row of the data set contains interactions of a single user. All entries are ids.
     * user_a interaction_1 interaction_2 interaction_3...
     * user_b interaction_1 interaction_2 interaction_3...
     * @param filepath Path to input file
     * @return A two dimensional array representing the parsed input file.
     * @throws IOException if path is not readable
     */
    public static int[][] readRowBased(String filepath) throws IOException {
        logger.info("Reading from file: " + filepath);
        int numLines = numberOfLines(new FileReader(filepath));
        int[][] lines = new int[numLines][];

        BufferedReader bufReader = new BufferedReader(new FileReader(filepath));
        String line;
        int lineNumber = 0;
        while ((line = bufReader.readLine()) != null) {
            String[] split = line.split("\t");
            lines[lineNumber] = new int[split.length];
            for (int i = 0; i < split.length; ++i)
                lines[lineNumber][i] = Integer.parseInt(split[i]);
            ++lineNumber;
            if (lineNumber % 1000000 == 0)
                logger.info(String.format("Reading line: %d", lineNumber));

        }
        bufReader.close();
        logger.info(String.format("read %d datapoints", lineNumber));
        return lines;
    }

    /**
     * Randomly splits rows of an input file into two files based on a given ratio.
     * @param input A reader representing the input file
     * @param output1 A writer for the first part
     * @param output2 A writer for the second part
     * @param ratio Ratio to split. 0.5 = 50%
     * @throws IOException if writer is not writable
     */
    public static void splitToFile(Reader input, Writer output1, Writer output2, double ratio) throws IOException {
        PrintWriter writer1 = new PrintWriter(new BufferedWriter(output1), false);
        PrintWriter writer2 = new PrintWriter(new BufferedWriter(output2), false);

        BufferedReader bufReader = new BufferedReader(input);
        String line;
        int lineNumber = 0;
        Random generator = new Random();
        while ((line = bufReader.readLine()) != null) {
            ++lineNumber;
            if (lineNumber % 10000000 == 0)
                logger.info(String.format("Reading line: %d", lineNumber));
            if (generator.nextDouble() < ratio)
                writer1.println(line);
            else
                writer2.println(line);
        }
        bufReader.close();
        writer1.close();
        writer2.close();
    }

    /**
     * Returns number of lines for an input reader.
     * @param input A reader
     * @return Number of lines.
     * @throws IOException if reader is not readable
     */
    public static int numberOfLines(Reader input) throws IOException {
        BufferedReader bufReader = new BufferedReader(input);
        int lineNumber = 0;
        while (bufReader.readLine() != null)
            ++lineNumber;
        return lineNumber;
    }

    /**
     * Writes latens vectors to a given writer
     * @param latentVectors A mapping from id to a latent vector
     * @param writer A writer for the output
     * @throws IOException if writer is not writable
     */
    public static void write(Map<Integer, FloatMatrix> latentVectors, Writer writer) throws IOException {
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(writer), false);
        latentVectors.entrySet().stream().parallel().map(entry -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (Float value : entry.getValue().toArray())
                stringBuilder.append(String.format(" %.4f", value));
            return entry.getKey() + stringBuilder.toString();
        }).forEach(line -> printWriter.println(line));
        printWriter.close();
    }

    /**
     * Writes a vector of biases to an output file.
     * @param w1 One dimensionsal vector containing biases of a model.
     * @param writer A writer to write to
     * @throws IOException if writer is not writable
     */
    public static void write(FloatMatrix w1, Writer writer) throws IOException {
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(writer), false);
        for (int i = 0; i < w1.length; ++i)
            printWriter.println(String.format("%.4f ", w1.get(i)));
        printWriter.close();
    }

    /**
     * Reads in latent vectors from an input file.
     * @param input Input reader.
     * @return A mapping from item id to its latent vector
     * @throws IOException if input is not readable
     */
    public static Map<Integer, FloatMatrix> readLatentVectors(Reader input) throws IOException {
        BufferedReader bufReader = new BufferedReader(input);
        Map<Integer, FloatMatrix> latentVectors = new HashMap<>();
        String line;
        while ((line = bufReader.readLine()) != null) {
            if (!line.startsWith("#")) {
                String[] split = line.split(" ");
                int id = Integer.parseInt(split[0]);
                float[] elements = new float[split.length - 1];
                for (int i = 1; i < split.length; ++i)
                    elements[i - 1] = Float.parseFloat(split[i]);
                latentVectors.put(id, new FloatMatrix(elements));
            }
        }
        return latentVectors;
    }

    /**
     * Shuffles a given input data set.
     * @param input Input reader
     * @param output Output writer
     * @param bufferSize number of lines to buffer before shuffling and flushing to disk.
     * @throws IOException if writer is not writable
     */
    public static void shuffle(Reader input, Writer output, int bufferSize) throws IOException {
        BufferedReader bufReader = new BufferedReader(input);
        BufferedWriter bufWriter = new BufferedWriter(output);

        List<String> lines = new ArrayList<>();
        String line;
        int i = 0;
        while (true) {
            line = bufReader.readLine();
            if (i > bufferSize || line == null) {
                Collections.shuffle(lines);
                for (String out : lines) {
                    bufWriter.append(out);
                    bufWriter.newLine();
                }
                if (line == null) {
                    bufReader.close();
                    bufWriter.close();
                    return;
                }
                i = 0;
                lines.clear();
            }
            lines.add(line);
            i++;
        }
    }

    /**
     * Reads in additional side information per item.
     * The format is in sparse vector format:
     * ITEM_ID FEATURE_ID:FEATURE_VALUE FEATURE_ID:FEATURE_VALUE...
     * @param input Path to input reader
     * @return A mapping from item id to a sparse vector
     * @throws IOException if reader is not readable
     */
    public static Map<Integer, SparseVector> readFeatures(Reader input) throws IOException {
        Map<Integer, SparseVector> itemFeatures = new HashMap<>();
        BufferedReader bufReader = new BufferedReader(input);
        String line;
        while ((line = bufReader.readLine()) != null) {
            String[] split = line.split("\t");
            int itemId = Integer.parseInt(split[0]);
            SparseVector sparseVector = new SparseVector(split.length - 1);
            for (int i = 1; i < split.length; ++i) {
                String[] sparseEntry = split[i].split(":");
                sparseVector.set(i - 1, Integer.parseInt(sparseEntry[0]), Float.parseFloat(sparseEntry[1]));
            }
            itemFeatures.put(itemId, sparseVector);
        }
        return itemFeatures;
    }

}
