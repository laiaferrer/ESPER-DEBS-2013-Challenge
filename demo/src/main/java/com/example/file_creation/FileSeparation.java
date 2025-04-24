package com.example.file_creation;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileSeparation {
    public static void main(String[] args) {
        String inputFile = "full-game.txt"; // Adjust file path
        Map<String, BufferedWriter> writers = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    String identifier = parts[0];

                    // Get or create a writer for this identifier
                    BufferedWriter writer = writers.computeIfAbsent(identifier, id -> {
                        try {
                            return new BufferedWriter(new FileWriter("sensor_" + id + ".csv"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    // Write the line directly to the file
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close all writers to free memory
            for (BufferedWriter writer : writers.values()) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
