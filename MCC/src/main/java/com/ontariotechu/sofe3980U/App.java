package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Evaluate Multiclass Classification
 */
public class App {
    public static void main(String[] args) {
        String filePath = "model.csv";
        double crossEntropy = 0;
        int totalSamples = 0;
        Map<Integer, Map<Integer, Integer>> confusionMatrix = new HashMap<Integer, Map<Integer, Integer>>();

        try {
            FileReader filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
            List<String[]> allData = csvReader.readAll();

            for (String[] row : allData) {
                int y_true = Integer.parseInt(row[0]);

                // Find the predicted class (argmax of probabilities)
                int predictedClass = 1;
                double maxProb = Double.parseDouble(row[1]);

                for (int i = 2; i < row.length; i++) {
                    double prob = Double.parseDouble(row[i]);
                    if (prob > maxProb) {
                        maxProb = prob;
                        predictedClass = i; // Index represents the class
                    }
                }

                // Compute Cross-Entropy (CE)
                crossEntropy += -Math.log(maxProb + 1e-10);

                // Update Confusion Matrix
                confusionMatrix.putIfAbsent(y_true, new HashMap<Integer, Integer>());
                confusionMatrix.get(y_true).put(predictedClass, confusionMatrix.get(y_true).getOrDefault(predictedClass, 0) + 1);

                totalSamples++;
            }

            // Final CE computation
            crossEntropy /= totalSamples;

            // Print Results
            System.out.printf("Cross-Entropy (CE): %.5f\n", crossEntropy);
            System.out.println("\nConfusion Matrix:");
            printConfusionMatrix(confusionMatrix);

        } catch (IOException e) {
            System.out.println("Error reading the CSV file: " + filePath);
        }
    }

    /**
     * Print the Confusion Matrix
     */
    private static void printConfusionMatrix(Map<Integer, Map<Integer, Integer>> confusionMatrix) {
        System.out.println("True Label \\ Predicted Label");
        for (int actual : confusionMatrix.keySet()) {
            System.out.print(actual + ": ");
            for (int predicted : confusionMatrix.get(actual).keySet()) {
                System.out.print(predicted + "(" + confusionMatrix.get(actual).get(predicted) + ") ");
            }
            System.out.println();
        }
    }
}
