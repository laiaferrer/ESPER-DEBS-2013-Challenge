package com.example;

import com.espertech.esper.client.*;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;


import java.util.*;
import java.util.concurrent.*;
import com.example.AsyncLogger;


public class Query3 {
    private final EPAdministrator admin;
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private final AsyncLogger logger;

    public Query3(EPAdministrator admin) {
        this.admin = admin;
        this.logger = new AsyncLogger("query3.txt", logQueue);
    }

    public void startListening(EPServiceProvider epService) {
        //new Query3_2(epService.getEPAdministrator()).startListening(epService);
        //new HeatmapVisualization(epService.getEPAdministrator());

        new Query3_2(epService.getEPAdministrator(), logQueue).startListening(epService);

        String contextEPL = "create context PlayerContext partition by player_id from SensorEvent";
        admin.createEPL(contextEPL);

        
        String eplQuery =   "context PlayerContext " +
                            "select prev(1, ts) as prev_ts, ts, prev(1, player_id) as player_id, prev(1, x) as prev_x, prev(1, y) as prev_y " +
                            "from SensorEvent.win:length(2) " +
                            "where sid NOT IN ('4', '8', '10', '12', '105', '106') " +
                            "group by player_id ";

        EPStatement statement = admin.createEPL(eplQuery);

        statement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    String playerId = (String) event.get("player_id");
                    Object x = event.get("prev_x");
                    Object y = event.get("prev_y");
                    long ts = (Long) event.get("ts");
                    long prev_ts = (Long) event.get("prev_ts");
                    long tsMillis = ts / 1_000_000;      // Convert from ps to ms
                    long prevTsMillis = prev_ts / 1_000_000;

                    //System.out.println("PLAYER_ID: " + playerId + " TS: " + prev_ts);

                    if (x != null && y != null && inside_court((double) x, (double) y)) {
                        updatePlayerCellTime(playerId, (double) x, (double) y, tsMillis, prevTsMillis, epService);
                    }
                }
            }
        });
    }

    private static boolean inside_court(double x, double y) {
        return x >= 0 && x <= 52489 && y <= 33965 && y >= -33960;
    }

    private void updatePlayerCellTime(String playerId, double x, double y, long ts, long prev_ts, EPServiceProvider epService) {
        if (ts == prev_ts) return;

        int gridRows = 64;
        int gridCols = 100;

        double rowHeight = 67.92 / gridRows;
        double x_cell = Math.floor((x / 1000) / (52.47 / gridCols));
        double y_cell = (y >= 0) ?
                Math.floor((33.96 - (y / 1000)) / rowHeight) :
                Math.floor(((-1 * y / 1000) + 33.96) / rowHeight);

        long duration = ts - prev_ts;
        HeatMapEvent event = new HeatMapEvent(prev_ts, playerId, x_cell, y_cell, duration);
        epService.getEPRuntime().sendEvent(event);
        //System.out.println("event send");
    }
}
