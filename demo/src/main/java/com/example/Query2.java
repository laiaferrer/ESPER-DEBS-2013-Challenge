package com.example;

import java.util.Comparator;
import java.util.List;
import java.util.Map;


import com.RunningStatistics;
import com.espertech.esper.client.*;
import com.example.EventSender;
import com.example.BallPossessionPlayer;


public class Query2 {
    private EPAdministrator admin;
    private String currentSid;
    private String lastPlayerSid;

    public Query2(EPAdministrator admin) {
        this.admin = admin;
        currentSid = "";
        lastPlayerSid = "";
    }

    public void startListening(EPServiceProvider epService) {
        
        Query2_2 query2_2 = new Query2_2(epService.getEPAdministrator());
        query2_2.startListening(epService);

        Configuration config = new Configuration();
        config.addEventType("ShotEvent", ShotEvent.class.getName());

        String createPlayerWindow = "create window LastPlayerEvent.std:unique(sid) as SensorEvent";

        admin.createEPL(createPlayerWindow);

        String insertPlayers = "insert into LastPlayerEvent select * from SensorEvent where sid NOT IN ('4', '8', '10', '12', '105', '106')";
        admin.createEPL(insertPlayers);

        /*String printPlayerEvents = "select * from LastPlayerEvent";
        EPStatement printPlayerStatement = admin.createEPL(printPlayerEvents);

        // Add a listener to print the player events when they are inserted into the window
        printPlayerStatement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    // Print the details of the inserted player event
                    System.out.println("Inserted Player Event: " +
                        "sid: " + event.get("sid") +
                        " ts: " + event.get("ts") +
                        " x: " + event.get("x") +
                        " y: " + event.get("y") +
                        " z: " + event.get("z"));
                }
            }
        });*/

        /*String countPlayerEvents = "select count(*) as eventCount from LastPlayerEvent";
        EPStatement countPlayerStatement = admin.createEPL(countPlayerEvents);

        // Add a listener to print the number of elements in the window whenever an event is added
        countPlayerStatement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    // Print the count of events in the window
                    System.out.println("Number of Player Events in the Window: " + event.get("eventCount"));
                }
            }
        });*/

        String createBallWindow = "create window NotHitLastBallEvent.win:length(1) as BallEvent";
        admin.createEPL(createBallWindow);

        String insertBallEvents = "insert into NotHitLastBallEvent " +
                                  "select * " +
                                  "from BallEvent " +
                                  "where sid = '4' or sid = '8' or sid = '10' or sid = '12'";

        admin.createEPL(insertBallEvents);

        /*String printBallEvents = "select * from NotHitLastBallEvent";
        EPStatement printBallStatement = admin.createEPL(printBallEvents);

        // Add a listener to print the ball events when they are inserted into the window
        printBallStatement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    // Print the details of the inserted ball event
                    System.out.println("Inserted Ball Event: " +
                        "sid: " + event.get("sid") +
                        " ts: " + event.get("ts") +
                        " x: " + event.get("x") +
                        " y: " + event.get("y") +
                        " z: " + event.get("z"));
                }
            }
        });*/

        String detectAndPredictShot =   "select b.sid as sid, p.sid as PlayerSid,  b.ts as ts, p.player_id as player_id, b.x as x, b.y as y, b.z as z, b.v as v, b.vx as vx, b.vy as vy, b.vz as vz, b.a as a, b.ax as ax, b.ay as ay, b.az as az, p.team_id as team_id, p.ts as player_ts " +
                                        "from LastPlayerEvent p, NotHitLastBallEvent b " +
                                        "where (p.x - b.x) * (p.x - b.x) + (p.y - b.y) * (p.y - b.y) + (p.z - b.z) * (p.z - b.z) <= 1000000 " +  
                                        "and b.a >= 55000000 " +
                                        "and b.ts > p.ts " +
                                        "and b.x >= 0 and b.x <= 52489 and b.y >= -33960 and b.y <= 33965  " +
                                        "order by ts asc " +
                                        "limit 1";

        EPStatement statement = admin.createEPL(detectAndPredictShot);

        statement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    //System.out.println("EVENT: sid: " + event.get("sid") + " Ball_ts: " + event.get("ts") + " player_id: " + event.get("player_id"));
                    //send event
                    if (lastPlayerSid != (String) event.get("player_id")) {
                        lastPlayerSid = (String) event.get("player_id");
                        currentSid = (String) event.get("sid");
                        double x = (double) event.get("x");
                        double y = (double) event.get("y");
                        long player_ts = (long) event.get("player_ts");

                        if(x >= 0 && x <= 52489 && y >= -33960 && y <= 33965) {
                            
                            ShotEvent event1 = new ShotEvent(
                            (String) event.get("sid"),
                            (String) event.get("PlayerSid"),
                            (long) event.get("ts"),
                            (String) event.get("player_id"),
                            (double) event.get("x"),
                            (double) event.get("y"),
                            (double) event.get("z"),
                            (double) event.get("v"),
                            (double) event.get("vx"),
                            (double) event.get("vy"),
                            (double) event.get("vz"),
                            (double) event.get("a"),
                            (double) event.get("ax"),
                            (double) event.get("ay"),
                            (double) event.get("az"),
                            (String) event.get("team_id")
                            );
                            epService.getEPRuntime().sendEvent(event1);
                            System.out.println("EVENT SENDED WITH BALLTS: " + (long) event.get("ts") + " PLAYER_TS: " + player_ts + " BALLSID: " + (String) event.get("sid") + " PLAYERTS: " + (long) event.get("player_ts"));
                        } else {
                            //System.out.println("A shot has been detected but not sended because it is off court");
                        }
                    } else {
                        //System.out.println("A shot has been detected but not sended because it is from the same player");
                    }
                }
            }
        });

        String removePlayerAfterShot = 
            "on ShotEvent se delete from LastPlayerEvent p where p.sid = se.playerSid";
        admin.createEPL(removePlayerAfterShot);


        //to stop the possession if the ball goes off court
        String offCourt =   "select b.sid as sid, b.ts as ts, b.x as x, b.y as y, b.z as z, b.v as v, b.vx as vx, b.vy as vy, b.vz as vz, b.a as a, b.ax as ax, b.ay as ay, b.az as az " +
                            "from NotHitLastBallEvent b " +
                            "where b.x  < 0 or b.x > 52489 or b.y > 33965 or b.y < -33960 ";

        EPStatement statement2 = admin.createEPL(offCourt);

        statement2.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    if ((String) event.get("sid") == currentSid) {
                        currentSid = "";
                        lastPlayerSid = "";
                        //System.out.println("THE BALL WENT OFF COURT: sid: " + (String) event.get("sid") + " ts: " + (long) event.get("ts") + " x,y,z: " + (Double) event.get("x") +" , " + (Double) event.get("y") + " , " + (Double) event.get("z"));
                        //send event
                        ShotEvent event1 = new ShotEvent(
                            (String) event.get("sid"),
                            "off",
                            (long) event.get("ts"),
                            "off",
                            (double) event.get("x"),
                            (double) event.get("y"),
                            (double) event.get("z"),
                            (double) event.get("v"),
                            (double) event.get("vx"),
                            (double) event.get("vy"),
                            (double) event.get("vz"),
                            (double) event.get("a"),
                            (double) event.get("ax"),
                            (double) event.get("ay"),
                            (double) event.get("az"),
                            " "
                        );
                        epService.getEPRuntime().sendEvent(event1);

                    }
                }
            }
        });

        String removeHitBall =  "on ShotEvent se " +
                                "delete from NotHitLastBallEvent b where b.sid = se.ballSid and se.ts =b.ts";

        admin.createEPL(removeHitBall);
    }
}
