package com.example;

import com.example.BallPossessionTeam;


public class ClosestPlayerUtils {
    // UDF method to get the closest player ID
    /*public static String getClosestPlayerId(SensorEvent ballEvent) {
        // Call the method in Query2 and return the result
        return Query2.getClosestPlayerId(ballEvent);
    }*/

    // UDF method to get the closest distance
    public static Double getDistance(double playerX, double playerY, double playerZ) {
        SensorEvent ballEvent = EventSender.BallPosition;

        double ballX = ballEvent.getX();
        double ballY = ballEvent.getY();
        double ballZ = ballEvent.getZ();

        // Calculate Euclidean distance between the player and the ball
        double distance = Math.sqrt(Math.pow(playerX - ballX, 2) + Math.pow(playerY - ballY, 2) + Math.pow(playerZ - ballZ, 2));

        return distance;
    }
}
