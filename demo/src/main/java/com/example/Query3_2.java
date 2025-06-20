package com.example;

import com.espertech.esper.client.*;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Query3_2 {
    private final EPAdministrator admin;
    private final BlockingQueue<String> logQueue;

    private final Map<String, Long> totalTimeMap1min = new ConcurrentHashMap<>();
    private final Map<String, Long> totalTimeMap5min = new ConcurrentHashMap<>();
    private final Map<String, Long> totalTimeMap10min = new ConcurrentHashMap<>();
    private final Map<String, Long> totalTimeMapFull = new ConcurrentHashMap<>();

    private final Map<String, LogEvent> lastUpdates = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Query3_2(EPAdministrator admin, BlockingQueue<String> logQueue) {
        this.admin = admin;
        this.logQueue = logQueue;

        // Schedule log flushing every second
        scheduler.scheduleAtFixedRate(this::flushUpdatesToLog, 1, 1, TimeUnit.SECONDS);
    }

    public void startListening(EPServiceProvider epService) {
        admin.getConfiguration().addEventType(HeatMapEvent.class);

        setupWindowedHeatMap(60, "1min", totalTimeMap1min);
        setupWindowedHeatMap(300, "5min", totalTimeMap5min);
        setupWindowedHeatMap(600, "10min", totalTimeMap10min);
        setupFullGameHeatMap("full", totalTimeMapFull);
    }

    private void setupWindowedHeatMap(int durationSec, String label, Map<String, Long> totalTimeMap) {
        String window = String.format("win:ext_timed(ts, %d sec)", durationSec);

        String totalTimeEPL = String.format(
            "select sum(duration) as total_time, ts, playerId from HeatMapEvent.%s group by playerId", window);
        String cellTimeEPL = String.format(
            "select sum(duration) as total_time_cell, ts, playerId, cellX, cellY from HeatMapEvent.%s group by playerId, cellX, cellY", window);

        EPStatement totalTimeStmt = admin.createEPL(totalTimeEPL);
        EPStatement cellTimeStmt = admin.createEPL(cellTimeEPL);

        totalTimeStmt.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    String playerId = (String) event.get("playerId");
                    long totalTime = (Long) event.get("total_time");
                    totalTimeMap.put(playerId, totalTime);
                }
            }
        });

        cellTimeStmt.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    bufferCellEvent(event, label, totalTimeMap);
                }
            }
        });
    }

    private void setupFullGameHeatMap(String label, Map<String, Long> totalTimeMap) {
        String totalTimeEPL = "select sum(duration) as total_time, ts, playerId from HeatMapEvent.win:keepall() group by playerId";
        String cellTimeEPL = "select sum(duration) as total_time_cell, ts, playerId, cellX, cellY from HeatMapEvent.win:keepall() group by playerId, cellX, cellY";

        EPStatement totalTimeStmt = admin.createEPL(totalTimeEPL);
        EPStatement cellTimeStmt = admin.createEPL(cellTimeEPL);

        totalTimeStmt.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    String playerId = (String) event.get("playerId");
                    long totalTime = (Long) event.get("total_time");
                    totalTimeMap.put(playerId, totalTime);
                }
            }
        });

        cellTimeStmt.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    bufferCellEvent(event, label, totalTimeMap);
                }
            }
        });
    }

    private void bufferCellEvent(EventBean event, String label, Map<String, Long> totalTimeMap) {
        String playerId = (String) event.get("playerId");
        double cellX = (Double) event.get("cellX");
        double cellY = (Double) event.get("cellY");
        long ts = (Long) event.get("ts");
        long totalTimeCell = (Long) event.get("total_time_cell");

        Long totalTime = totalTimeMap.get(playerId);
        if (totalTime == null || totalTime == 0) return;

        double percentage = ((double) totalTimeCell / totalTime) * 100;
        String formattedPercentage = String.format("%.2f", percentage);

        int gridRows = 64;
        int gridCols = 100;

        StringBuilder sb = new StringBuilder();
        sb.append("HEAT MAP").append(System.lineSeparator());
        sb.append("Window Type: ").append(label).append(System.lineSeparator());
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

        String key = playerId + "_" + cellX + "_" + cellY + "_" + label;
        int priority = getPriority(label);
        lastUpdates.put(key, new LogEvent(ts, sb.toString(), priority));
    }

    private int getPriority(String label) {
        return switch (label) {
            case "1min" -> 1;
            case "5min" -> 2;
            case "10min" -> 3;
            case "full" -> 4;
            default -> 99;
        };
    }

    private void flushUpdatesToLog() {
        lastUpdates.values().stream()
            .sorted((a, b) -> {
                int cmp = Long.compare(a.ts, b.ts);
                return (cmp != 0) ? cmp : Integer.compare(a.priority, b.priority);
            })
            .forEach(event -> logQueue.offer(event.message));
        lastUpdates.clear();
    }

    private static class LogEvent {
        final long ts;
        final String message;
        final int priority;

        LogEvent(long ts, String message, int priority) {
            this.ts = ts;
            this.message = message;
            this.priority = priority;
        }
    }
}
