package com.example;

import com.espertech.esper.client.*;

public class ThresholdCalculator {

    private final EPAdministrator admin;
    private final int THRESHOLD = 100000;  // Print after every 10,000 events
    private long sensorEventCount = 0;  // Track the number of SensorEvents
    private long ballEventCount = 0;    // Track the number of BallEvents
    private long startTime = 0;         // Store the start time of the event counting

    public ThresholdCalculator(EPAdministrator admin) {
        this.admin = admin;
    }

    public void startListening(EPServiceProvider epService) {
        // Query to listen for SensorEvents
        String sensorEventQuery = "select * from SensorEvent";
        EPStatement sensorEventStatement = admin.createEPL(sensorEventQuery);

        // Query to listen for BallEvents
        String ballEventQuery = "select * from BallEvent";
        EPStatement ballEventStatement = admin.createEPL(ballEventQuery);

        // Listener for SensorEvents
        sensorEventStatement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    sensorEventCount++;  // Increment the sensor event count
                    printEventRate();
                }
            }
        });

        // Listener for BallEvents
        ballEventStatement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    ballEventCount++;  // Increment the ball event count
                    printEventRate();
                }
            }
        });
    }

    private void printEventRate() {
        // Calculate the total events received
        long totalEventCount = sensorEventCount + ballEventCount;

        long currentTime = System.currentTimeMillis();
        if (startTime == 0) {
            startTime = currentTime;
        }

        long elapsedTime = currentTime - startTime;  // Time in milliseconds

        // Calculate the rate in events per second
        double eventsPerSecond = (totalEventCount * 1000.0) / elapsedTime;

        // Print the rate after every THRESHOLD events
        if (totalEventCount % THRESHOLD == 0) {
            System.out.println("Total events processed: " + totalEventCount);
            System.out.println("Sensor Events: " + sensorEventCount + ", Ball Events: " + ballEventCount);
            System.out.println("Ingestion Rate: " + eventsPerSecond + " events/sec");

            // Optionally reset or adjust timing here
            startTime = currentTime;  // Reset the timer after printing
            sensorEventCount = 0;     // Reset event count after print
            ballEventCount = 0;       // Reset ball event count after print
        }
    }
}
