package com.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HeatMapPrinter extends Thread {
    private final String fileName;
    private final Map<String, Map<String, HeatMapData>> updatedData = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    public HeatMapPrinter(String fileName) {
        this.fileName = fileName;
        setDaemon(true);
        start(); // Start thread immediately
    }

    /**
     * Store the latest update per (playerId, cellKey) pair.
     */
    /*public void updateCell(String playerId, String cellKey, String heatMapText, long ts) {
        updatedData
            .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
            .put(cellKey, new HeatMapData(ts, heatMapText));
    }*/

    public void updateCell(String playerId, String cellKey, String heatMapText, long ts) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write(heatMapText);
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1000); // Wait 1 second
                flushUpdates();     // Then flush updates
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }

    /**
     * Collect all cell updates, sort them by timestamp, and write to file.
     */
    private void flushUpdates() {
        List<HeatMapData> allUpdates = new ArrayList<>();
        for (Map<String, HeatMapData> playerMap : updatedData.values()) {
            allUpdates.addAll(playerMap.values());
        }

        allUpdates.sort(Comparator.comparingLong(d -> d.ts));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            for (HeatMapData data : allUpdates) {
                writer.write(data.heatMapText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        updatedData.clear();
    }
}
