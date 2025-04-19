package com.example;

import com.espertech.esper.client.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import com.example.AsyncLogger;


public class Query3_2 {
    private final EPAdministrator admin;
    private final BlockingQueue<String> logQueue;
    final Map<String, Long> cellDurationMap = new ConcurrentHashMap<>();
    final Map<String, Double[]> cellCoordinates = new ConcurrentHashMap<>();
    private final Map<String, Long> totalTimeMap = new ConcurrentHashMap<>();

    public Query3_2(EPAdministrator admin, BlockingQueue<String> logQueue) {
        this.admin = admin;
        this.logQueue = logQueue;
    }

    public void startListening(EPServiceProvider epService) {
        admin.getConfiguration().addEventType(HeatMapEvent.class);

        String eplQuery = "select sum(duration) as total_time, ts, playerId " +
                "from HeatMapEvent.win:time(5 min) " +
                "group by playerId ";

        String eplQuery2 = "select sum(duration) as total_time_cell, playerId, cellX, cellY, ts " +
                "from HeatMapEvent.win:time(5 min) " +
                "group by playerId, cellX, cellY ";

        EPStatement statement = admin.createEPL(eplQuery);
        EPStatement statement2 = admin.createEPL(eplQuery2);

        statement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    String playerId = (String) event.get("playerId");
                    long totalTime = (Long) event.get("total_time");
                    long ts = (Long) event.get("ts");
                    //System.out.println("ADDED TO THE MAP: " + "PLAYER: " + playerId + " TOTAL TIME: " + totalTime);
                    totalTimeMap.put(playerId, totalTime);

                    //System.out.println("PLAYER_ID: " + playerId +  " TS: " + ts + " TOTAL TIME: " + totalTime);

                    /*for (Map.Entry<String, Long> entry : cellDurationMap.entrySet()) {
                        String key = entry.getKey();
                        if (!key.startsWith(playerId + "_" )) continue;
                        //System.out.println("KEYY: " + key);
                        long totalTimeCell = entry.getValue();
                        Double[] cellXY = cellCoordinates.get(key);

                        double percentage = ((double) totalTimeCell / totalTime) * 100;
                        String formattedPercentage = String.format("%.2f", percentage);

                        int gridRows = 64;
                        int gridCols = 100;

                        double cellX = cellXY[0];
                        double cellY = cellXY[1];

                        StringBuilder sb = new StringBuilder();
                        sb.append("HEAT MAP:").append(System.lineSeparator());
                        sb.append("Timestamp: ").append(ts).append(System.lineSeparator());
                        sb.append("Player ID: ").append(playerId).append(System.lineSeparator());
                        sb.append("Cell X1: ").append((cellX - 1) * (52.47 / gridCols)).append(System.lineSeparator());
                        sb.append("Cell Y1: ").append(cellY * (67.92 / gridRows)).append(System.lineSeparator());
                        sb.append("Cell X2: ").append(cellX * (52.47 / gridCols)).append(System.lineSeparator());
                        sb.append("Cell Y2: ").append((cellY - 1) * (67.92 / gridRows)).append(System.lineSeparator());
                        sb.append("Total time: ").append(totalTime).append(System.lineSeparator());
                        sb.append("Total Cell Time: ").append(totalTimeCell).append(System.lineSeparator());
                        sb.append("Percent Time in Cell: ").append(formattedPercentage).append(" %").append(System.lineSeparator());
                        sb.append("----------------------------").append(System.lineSeparator());


                        logQueue.offer(sb.toString());

                        //System.out.println("QUEUE SIZE: " + logQueue.size() + " SB: " + sb);

                    }*/
                }
            }
        });

        
        // Store total_time_cell info per player + cell
        statement2.addListener((newData, oldData) -> {
            if (newData != null) {

                for (EventBean event : newData) {
                    String playerId = (String) event.get("playerId");
                    double cellX = (Double) event.get("cellX");
                    double cellY = (Double) event.get("cellY");
                    long ts = (Long) event.get("ts");
                    long totalTimeCell = (Long) event.get("total_time_cell");

                    long totalTime = totalTimeMap.get(playerId);
                    double percentage = ((double) totalTimeCell / totalTime) * 100;
                    String formattedPercentage = String.format("%.2f", percentage);

                    int gridRows = 64;
                    int gridCols = 100;

                    StringBuilder sb = new StringBuilder();
                    sb.append("HEAT MAP:").append(System.lineSeparator());
                    sb.append("Timestamp: ").append(ts).append(System.lineSeparator());
                    sb.append("Player ID: ").append(playerId).append(System.lineSeparator());
                    sb.append("Cell X1: ").append((cellX - 1) * (52.47 / gridCols)).append(System.lineSeparator());
                    sb.append("Cell Y1: ").append(cellY * (67.92 / gridRows)).append(System.lineSeparator());
                    sb.append("Cell X2: ").append(cellX * (52.47 / gridCols)).append(System.lineSeparator());
                    sb.append("Cell Y2: ").append((cellY - 1) * (67.92 / gridRows)).append(System.lineSeparator());
                    sb.append("Total time: ").append(totalTime).append(System.lineSeparator());
                    sb.append("Total Cell Time: ").append(totalTimeCell).append(System.lineSeparator());
                    sb.append("Percent Time in Cell: ").append(formattedPercentage).append(" %").append(System.lineSeparator());
                    sb.append("----------------------------").append(System.lineSeparator());


                    logQueue.offer(sb.toString());


                    //System.out.println("PLAYER_ID: " + playerId + " CELL X: " + cellX + " CELL Y: " + cellY + " TS: " + ts + " TOTAL TIME: " + totalTimeCell);
                    //System.out.println("CELL DURATION MAP LENGTH: " + cellDurationMap.size());
                    //System.out.println("CELL COORDINATES MAP LENGTH: " + cellCoordinates.size());
                    /*String key = playerId  + "_" + cellX + "_" + cellY;
                    cellDurationMap.put(key, totalTimeCell);
                    cellCoordinates.put(key, new Double[]{cellX, cellY});      */          
                    //System.out.println("KEY: " + key);
                }
            }
        });

    }
}
