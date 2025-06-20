package com.example;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class HeatMapTestDataGenerator {
    public static void main(String[] args) {
        String fileName = "heatmap_test_data.txt";
        int numEvents = 52477; // Generate 100 events

        long ts = 0L;
        double x = 0.0;
        double y = 33965.0;
        double z = 0.0;
        int sid = 44;
        long tsIncrement = 10_000_000_000L; // 10 seconds in picoseconds

        try (FileWriter writer = new FileWriter(fileName)) {
            DecimalFormat df = new DecimalFormat("0.000000");

            for (int i = 0; i < numEvents; i++) {
                String line = String.format("%d,%d,%s,%s,%s,0,0,0,0,0,0,0,0\n",
                        sid,
                        ts,
                        df.format(x),
                        df.format(y),
                        df.format(z)
                );


                writer.write(line);

                // update values
                ts += tsIncrement;
                x += 1.0;
                y -= 1.0;
            }

            System.out.println("✅ Test data written to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
