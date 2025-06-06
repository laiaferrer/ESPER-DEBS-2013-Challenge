package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.espertech.esper.client.*;

public class Query4_2 {
    private EPAdministrator admin;
    private String player_id;

    public Query4_2(EPAdministrator admin) {
        this.admin = admin;
        player_id = "";
    }

    public void startListening(EPServiceProvider epService) {
        epService.getEPAdministrator().getConfiguration().addEventType("GoalShotEvent", GoalShotEvent.class.getName());        
            
        String contextEPL = "create context GoalShotContext " +
                            "initiated by GoalShotEvent as a " +
                            "terminated by BallEvent(x < 1 or x > 52477 or y < -33960 or y > 33965 or " +
                            "(vy < 0 and x > 22578.5 and x < 29898.5 and y <= 33941.0 and y > 31441) " +
                            "or (a.vy > 0 and vy <= 0) or (a.vy < 0 and vy >= 0))";

        epService.getEPAdministrator().createEPL(contextEPL);

        String eplQuery =   "context GoalShotContext " +
                            "select context.a.playerId as player_id, ts, x, y, z, v, vx, vy, vz, a, ax, ay, az " +
                            "from BallEvent";

        EPStatement statement2 = admin.createEPL(eplQuery);

        String eplQuery1 =   "select * " +
                              "from BallEvent";

        EPStatement statement12 = admin.createEPL(eplQuery1);

        statement12.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    //System.out.println("Ball recieved with ts: " + event.get("ts"));
                }
            }
        });

        statement2.addListener((newData, oldData) -> {
            if (newData != null) {
                try {
                    // Ensure the file exists before writing
                    File file = new File("query4.txt");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    // Open FileWriter in append mode
                    FileWriter fileWriter = new FileWriter(file, true);
                    PrintWriter printWriter = new PrintWriter(fileWriter);

                    for (EventBean event : newData) {
                        //System.out.println("Ball eevnts during the Goal");
                        printWriter.println("----------------------------");
                        printWriter.println("ShotEvent: ts " + event.get("ts") + 
                                           " player_id: " + event.get("player_id") +  
                                           " x: " + event.get("x") + 
                                           " y: " + event.get("y") + 
                                           " z: " + event.get("z") + 
                                           " |v|: " + event.get("v") + 
                                           " vx: " + event.get("vx") + 
                                           " vy: " + event.get("vy") + 
                                           " vz: " + event.get("vz") + 
                                           " |a|: " + event.get("a") + 
                                           " ax: " + event.get("ax") + 
                                           " ay: " + event.get("ax") + 
                                           " ay: " + event.get("ay") + 
                                           " az: " + event.get("az"));
                        printWriter.println("----------------------------");
                    }

                    // Close PrintWriter and FileWriter
                    printWriter.close();
                    fileWriter.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
