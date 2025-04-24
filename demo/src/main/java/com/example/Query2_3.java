package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;

public class Query2_3 {
    private EPAdministrator admin;
    private long total_time;
    private long teamA_time;
    private long teamB_time;

    public Query2_3(EPAdministrator admin) {
        this.admin = admin;
        this.total_time = 0;
        this.teamA_time = 0;
        this.teamB_time = 0;
    }

    public void startListening(EPServiceProvider epService) {

        String totalTime = "select SUM(duration) as total from PlayerPosession.win:time(1 min)";
        EPStatement statement_1 = admin.createEPL(totalTime);
        statement_1.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    total_time = (long) event.get("total");
                }
            }
        });

        String totalTimeA = "select SUM(duration) as total_time from PlayerPosession.win:time(1 min) WHERE teamId = 'teamA'";
        EPStatement statement_1A = admin.createEPL(totalTimeA);
        statement_1A.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    teamA_time = (long) event.get("total_time");
                }
            }
        });

        String totalTimeB = "select SUM(duration) as total_time from PlayerPosession.win:time(1 min) WHERE teamId = 'teamB'";
        EPStatement statement_1B = admin.createEPL(totalTimeB);
        statement_1B.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    teamB_time = (long) event.get("total_time");
                }
            }
        });

        String eplQueryWindow1 = "select * from PlayerPosession.win:time(1 min)";
        EPStatement statement1 = admin.createEPL(eplQueryWindow1);
        statement1.addListener((newData, oldData) -> {
            if (newData != null) {
                try {
                    // Ensure the file exists before writing
                    File file = new File("query2.txt");
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    // Open FileWriter in append mode
                    FileWriter fileWriter = new FileWriter(file, true);
                    PrintWriter printWriter = new PrintWriter(fileWriter);

                    for (EventBean event : newData) {
                        long ts = (long) event.get("ts");
                        String teamId = (String) event.get("teamId");
                        if(teamId != " ") {
                            long percent = (teamId.equals("teamA")) ? (teamA_time * 100 / total_time) : (teamB_time * 100 / total_time);

                            // Write to the file
                            printWriter.println("----------------------------");
                            printWriter.println("Updated Team Possession:");
                            printWriter.printf("Timestamp: %d%n", ts);
                            printWriter.printf("Team ID: %s%n", teamId);
                            printWriter.printf("Time: %d%n", teamId.equals("teamA") ? teamA_time : teamB_time);
                            printWriter.printf("Time Percent: %d%%%n", percent);
                            printWriter.println("----------------------------");
                            printWriter.println();
                        }
                        
                    }

                    // Close PrintWriter and FileWriter
                    printWriter.close();
                    fileWriter.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Define additional statements (for 5 min and 20 min windows)
        String eplQueryWindow2 = "select * from PlayerPosession.win:time(5 min)";
        EPStatement statement2 = admin.createEPL(eplQueryWindow2);

        String eplQueryWindow3 = "select * from PlayerPosession.win:time(20 min)";
        EPStatement statement3 = admin.createEPL(eplQueryWindow3);
    }
}
