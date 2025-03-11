package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.IOException;

/**
 * Evaluate Single Variable Continuous Regression
 */
public class App {
    public static void main(String[] args) {
        String[] filePaths = {"model_1.csv", "model_2.csv", "model_3.csv"};
        double bestMSE = Double.MAX_VALUE;
        String bestModel = "";

        for (String filePath : filePaths) {
            System.out.println("\nEvaluating model: " + filePath);
            double mse = 0, mae = 0, mare = 0;
            int count = 0;

            try {
                FileReader filereader = new FileReader(filePath);
                CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
                List<String[]> allData = csvReader.readAll();
                
                for (String[] row : allData) {
                    double y_true = Double.parseDouble(row[0]);
                    double y_pred = Double.parseDouble(row[1]);
                    double error = y_true - y_pred;

                    mse += Math.pow(error, 2);
                    mae += Math.abs(error);
                    mare += Math.abs(error) / (Math.abs(y_true) + 1e-10); // Avoid division by zero

                    count++;
                }

                if (count > 0) {
                    mse /= count;
                    mae /= count;
                    mare = (mare / count) * 100; // Convert to percentage
                }

                System.out.printf("MSE: %.5f, MAE: %.5f, MARE: %.5f%%\n", mse, mae, mare);

                if (mse < bestMSE) {
                    bestMSE = mse;
                    bestModel = filePath;
                }

            } catch (IOException e) {
                System.out.println("Error reading the CSV file: " + filePath);
            }
        }

        System.out.println("\nThe model with the lowest error is: " + bestModel);
    }
}
