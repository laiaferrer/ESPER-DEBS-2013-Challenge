package com.example;

public class ClosestPlayerUtils {
    // UDF method to get the closest player ID
    public static String getClosestPlayerId(SensorEvent ballEvent) {
        // Call the method in Query2 and return the result
        return Query2.getClosestPlayerId(ballEvent);
    }

    // UDF method to get the closest distance
    public static Double getClosestDistance(SensorEvent ballEvent) {
        // Call the method in Query2 and return the result
        return Query2.getClosestDistance(ballEvent);
    }
}
