package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.espertech.esper.client.*;

public class Query2_2 {
    private EPAdministrator admin;
    private Map<String, PlayerPosession> playerPossessionMap = new HashMap<>();

    public Query2_2(EPAdministrator admin) {
        this.admin = admin;
    }

    public void startListening(EPServiceProvider epService) {
        Query2_3 query2_3 = new Query2_3(epService.getEPAdministrator());
        query2_3.startListening(epService);

        Configuration config = new Configuration();
        config.addEventType("PlayerPosession", PlayerPosession.class.getName());

        String eplQuery = "select prev(1, ts) as prev_ts, ts, prev(1, playerId) as prev_playerId, prev(1, teamId) as prev_teamId " +
                          "from ShotEvent.win:length(2)";

        EPStatement statement = admin.createEPL(eplQuery);

        statement.addListener((newData, oldData) -> {
            if (newData != null) {
                try {
                    // Ensure the file exists before writing
                    File file = new File("query2.txt");
                    if (!file.exists()) {
                        file.createNewFile();  // Create the file if it doesn't exist
                    }

                    // Open FileWriter in append mode
                    FileWriter fileWriter = new FileWriter(file, true);
                    PrintWriter printWriter = new PrintWriter(fileWriter);

                    for (EventBean event : newData) {
                        long time = (long) event.get("ts") - (long) event.get("prev_ts");
                        String playerId = (String) event.get("prev_playerId");
                        String teamId = (String) event.get("prev_teamId");
                        long ts = (long) event.get("prev_ts");
                        int hits = 1;

                        if (teamId != " " && playerId != "off") {
                            PlayerPosession event1 = new PlayerPosession(ts, playerId, time, hits, teamId);

                            if (playerPossessionMap.containsKey(playerId)) {
                                // Update existing player data
                                PlayerPosession posession = playerPossessionMap.get(playerId);
                                posession.setTs(ts);
                                posession.setDuration(posession.getDuration() + time);
                                posession.setHits(posession.getHits() + hits);
                                hits = posession.getHits();
                                time = posession.getDuration();
                            } else {
                                // Insert new player data
                                playerPossessionMap.put(playerId, event1);
                            }

                            // Write output to the file instead of printing
                            printWriter.println("----------------------------");
                            printWriter.println("Updated Player Possession: ");
                            printWriter.printf("Timestamp: %d%n", ts);
                            printWriter.printf("Player ID: %s%n", playerId);
                            printWriter.printf("Team ID: %s%n", teamId);
                            printWriter.printf("Time: %d%n", time);
                            printWriter.printf("Hits: %d%n", hits);
                            printWriter.println("----------------------------");
                            printWriter.println();

                            // Send event
                            epService.getEPRuntime().sendEvent(event1);
                        }
                    }

                    // Close the PrintWriter and FileWriter
                    printWriter.close();
                    fileWriter.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
