package com.example;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.RunningStatistics;
import com.espertech.esper.client.*;
import com.example.EventSender;
import com.example.BallPossessionPlayer;
import com.example.BallPossessionTeam;


public class Query2 {
    private EPAdministrator admin;

    public Query2(EPAdministrator admin) {
        this.admin = admin;
        //admin.getConfiguration().addImport(ClosestPlayerUtils.class.getName());
        // Register the UDFs before using them in EPL queries
        admin.getConfiguration().addImport("com.example.ClosestPlayerUtils");
        admin.getConfiguration().addPlugInSingleRowFunction("getClosestPlayerId", "com.example.ClosestPlayerUtils", "getClosestPlayerId");
        admin.getConfiguration().addPlugInSingleRowFunction("getClosestDistance", "com.example.ClosestPlayerUtils", "getClosestDistance");
    }

    public static Double getClosestDistance(SensorEvent ballEvent) {
        String closestPlayerId = getClosestPlayerId(ballEvent);
        SensorEvent playerEvent = EventSender.PlayerPosition.get(closestPlayerId);

        double ballX = ballEvent.getX();
        double ballY = ballEvent.getY();
        double ballZ = ballEvent.getZ();

        double playerX = playerEvent.getX();
        double playerY = playerEvent.getY();
        double playerZ = playerEvent.getZ();

        // Calculate Euclidean distance between the player and the ball
        double distance = Math.sqrt(Math.pow(playerX - ballX, 2) + Math.pow(playerY - ballY, 2) + Math.pow(playerZ - ballZ, 2));

        return distance;
    }

    public static String getClosestPlayerId(SensorEvent ballEvent) {
        String closestPlayerId = null;
        double minDistance = Double.MAX_VALUE;  // Initialize with a very large number

        // Get the position of the ball (from ballEvent)
        double ballX = ballEvent.getX();
        double ballY = ballEvent.getY();
        double ballZ = ballEvent.getZ();

        for (Map.Entry<String, SensorEvent> entry : EventSender.PlayerPosition.entrySet()) {
            String playerId = entry.getKey();
            SensorEvent playerEvent = entry.getValue();

            // Get the player's position
            double playerX = playerEvent.getX();
            double playerY = playerEvent.getY();
            double playerZ = playerEvent.getZ();

            // Calculate Euclidean distance between the player and the ball
            double distance = Math.sqrt(Math.pow(playerX - ballX, 2) + Math.pow(playerY - ballY, 2) + Math.pow(playerZ - ballZ, 2));

            // Update the closest player if this player is closer
            if (distance < minDistance) {
                minDistance = distance;
                closestPlayerId = playerId;
            }
        }
        return closestPlayerId;  
    }

    public void startListening(EPServiceProvider epService) {

        String contextEPL = "create context BallPossessionContext " +
                            "partition by sid from SensorEvent(sid = '4' or sid = '8' or sid = '10' or sid = '12') " +
                            "initiated by SensorEvent(a > 55) as a " +        //to make sure that we are tracking the ball
                            "terminated by SensorEvent(a < 55) as endEvent " 
                            //"prev(endEvent.a) >= 55 and " + 
                            //"getClosestPlayerId(a) != getClosestPlayerId(endEvent) and " +
                            //"getClosestDistance(endEvent) < 1000"       //beacuse we should check that is closer than 1m but the values are given in mm
                            ;

        epService.getEPAdministrator().createEPL(contextEPL);

        // Define another EPL query, for example, detecting sudden speed changes
        String eplQuery = "context BallPossessionContext " +
                          "select * " +
                          "from SensorEvent ";

        EPStatement statement = admin.createEPL(eplQuery);


        statement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    System.out.println("Significant Speed Change Detected: ");
                }
            }
        });

        System.out.println("Query2 is listening...");
    }
}
