package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Evaluate Single Variable Binary Regression
 */
public class App {
    public static void main(String[] args) {
        String[] filePaths = {"model_1.csv", "model_2.csv", "model_3.csv"};
        double bestBCE = Double.MAX_VALUE;
        double bestAUC = 0;
        String bestModel = "";

        for (String filePath : filePaths) {
            System.out.println("\nEvaluating model: " + filePath);

            double bce = 0;
            int TP = 0, FP = 0, TN = 0, FN = 0;
            int count = 0;
            double threshold = 0.5; // Default threshold

            List<Double> trueLabels = new ArrayList<>();
            List<Double> predictedScores = new ArrayList<>();

            try {
                FileReader filereader = new FileReader(filePath);
                CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
                List<String[]> allData = csvReader.readAll();

                for (String[] row : allData) {
                    double y_true = Double.parseDouble(row[0]);
                    double y_pred = Double.parseDouble(row[1]);

                    trueLabels.add(y_true);
                    predictedScores.add(y_pred);

                    // Compute Binary Cross-Entropy (BCE)
                    if (y_true == 1) {
                        bce += -Math.log(y_pred + 1e-10);
                    } else {
                        bce += -Math.log(1 - y_pred + 1e-10);
                    }

                    // Convert predicted values to binary (0 or 1)
                    int y_pred_binary = (y_pred >= threshold) ? 1 : 0;

                    // Compute Confusion Matrix (TP, FP, TN, FN)
                    if (y_true == 1 && y_pred_binary == 1) TP++;
                    else if (y_true == 0 && y_pred_binary == 1) FP++;
                    else if (y_true == 0 && y_pred_binary == 0) TN++;
                    else if (y_true == 1 && y_pred_binary == 0) FN++;

                    count++;
                }

                // Final BCE computation
                if (count > 0) {
                    bce /= count;
                }

                // Compute Accuracy, Precision, Recall, and F1-score
                double accuracy = (double) (TP + TN) / (TP + TN + FP + FN);
                double precision = (TP + FP) > 0 ? (double) TP / (TP + FP) : 0;
                double recall = (TP + FN) > 0 ? (double) TP / (TP + FN) : 0;
                double f1_score = (precision + recall) > 0 ? (2 * precision * recall) / (precision + recall) : 0;

                // Compute AUC-ROC
                double auc = calculateAUC(trueLabels, predictedScores);

                System.out.printf("BCE: %.5f | Accuracy: %.5f | Precision: %.5f | Recall: %.5f | F1-score: %.5f | AUC-ROC: %.5f%n",
                        bce, accuracy, precision, recall, f1_score, auc);

                // Determine best model
                if (bce < bestBCE && auc > bestAUC) {
                    bestBCE = bce;
                    bestAUC = auc;
                    bestModel = filePath;
                }

            } catch (IOException e) {
                System.out.println("Error reading the CSV file: " + filePath);
            }
        }

        System.out.println("\nThe model with the best performance is: " + bestModel);
    }

    /**
     * Compute AUC-ROC using a threshold-based approach
     */
    private static double calculateAUC(List<Double> trueLabels, List<Double> predictedScores) {
        List<Double> sortedScores = new ArrayList<>(predictedScores);
        Collections.sort(sortedScores);

        List<Double> tprList = new ArrayList<>();
        List<Double> fprList = new ArrayList<>();

        int positiveCount = 0;
        int negativeCount = 0;
        for (double label : trueLabels) {
            if (label == 1) positiveCount++;
            else negativeCount++;
        }

        for (double threshold : sortedScores) {
            int TP = 0, FP = 0, TN = 0, FN = 0;

            for (int i = 0; i < predictedScores.size(); i++) {
                double y_true = trueLabels.get(i);
                double y_pred = predictedScores.get(i);

                int y_pred_binary = (y_pred >= threshold) ? 1 : 0;

                if (y_true == 1 && y_pred_binary == 1) TP++;
                else if (y_true == 0 && y_pred_binary == 1) FP++;
                else if (y_true == 0 && y_pred_binary == 0) TN++;
                else if (y_true == 1 && y_pred_binary == 0) FN++;
            }

            double TPR = positiveCount > 0 ? (double) TP / positiveCount : 0;
            double FPR = negativeCount > 0 ? (double) FP / negativeCount : 0;

            tprList.add(TPR);
            fprList.add(FPR);
        }

        // Compute Area Under Curve (AUC)
        double auc = 0;
        for (int i = 1; i < tprList.size(); i++) {
            auc += (tprList.get(i) + tprList.get(i - 1)) * Math.abs(fprList.get(i) - fprList.get(i - 1)) / 2;
        }

        return auc;
    }
}

