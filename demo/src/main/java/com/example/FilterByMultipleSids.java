package com.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FilterByMultipleSids {
    public static void main(String[] args) {
        // === CONFIGURATION ===
        String inputFile = "/mnt/c/ANTIGUO/Laia1/Laia/2024-2025/Thesis/Esper_Project/demo/filtered.csv";        // Input file path
        String outputFile = "filtered2.csv";    // Output file path

        // List of SIDs to keep
        Set<String> allowedSids = new HashSet<>(Arrays.asList("44"));

        // === FILTERING PROCESS ===
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFile));
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 13) continue;

                String sid = parts[0].trim();

                if (allowedSids.contains(sid)) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("Filtered events with selected sids saved to " + outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
