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

    public Query2(EPAdministrator admin) {
        this.admin = admin;
    }

    public void startListening(EPServiceProvider epService) {
        
        Query2_2 query2_2 = new Query2_2(epService.getEPAdministrator());
        query2_2.startListening(epService);

        Configuration config = new Configuration();
        config.addEventType("ShotEvent", ShotEvent.class.getName());


        String createBallWindow = "create window NotHitLastBallEvent.win:length(1) as BallEvent";
        admin.createEPL(createBallWindow);

        String insertBallEvents = "insert into NotHitLastBallEvent " +
                                  "select * " +
                                  "from BallEvent " +
                                  "where sid = '4' or sid = '8' or sid = '10' or sid = '12'";

        admin.createEPL(insertBallEvents);

        
        String detectAndPredictShot =   "select b.sid as sid, p.ts as ts, p.player_id as player_id, b.x as x, b.y as y, b.z as z, b.v as v, b.vx as vx, b.vy as vy, b.vz as vz, b.a as a, b.ax as ax, b.ay as ay, b.az as az, p.team_id as team_id " +
                                        "from Players p, NotHitLastBallEvent b " +
                                        "where (p.x - b.x) * (p.x - b.x) + (p.y - b.y) * (p.y - b.y) + (p.z - b.z) * (p.z - b.z) <= 1000000 " +  
                                        "and p.a >= 55000000 " +
                                        "and p.ts > b.ts";

        EPStatement statement = admin.createEPL(detectAndPredictShot);

        statement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    
                    //send event
                    ShotEvent event1 = new ShotEvent(
                        (String) event.get("sid"),
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

                    String removeHitBall =  "on ShotEvent se " +
                                            "delete from NotHitLastBallEvent where sid = se.sid";

                    admin.createEPL(removeHitBall);

                }
            }
        });

        //to stop the possession if the ball goes off court
        String offCourt =   "select b.sid as sid, b.ts as ts, b.x as x, b.y as y, b.z as z, b.v as v, b.vx as vx, b.vy as vy, b.vz as vz, b.a as a, b.ax as ax, b.ay as ay, b.az as az " +
                            "from NotHitLastBallEvent b " +
                            "where b.x  < 0 or b.x > 33941 or b.y > 33965 or b.y < -33960 ";

        EPStatement statement2 = admin.createEPL(offCourt);

        statement2.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    
                    //send event
                    ShotEvent event1 = new ShotEvent(
                        (String) event.get("sid"),
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

                    String removeHitBall =  "on ShotEvent se " +
                                            "delete from NotHitLastBallEvent where sid = se.sid";

                    admin.createEPL(removeHitBall);

                }
            }
        });
    }
}
