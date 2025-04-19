package com.example;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.example.*;
import com.espertech.esper.client.Configuration;



public class Query4 {
    private EPAdministrator admin;

    public Query4(EPAdministrator admin) {
        this.admin = admin;
    }   

    public void startListening(EPServiceProvider epService) {
        
        Configuration config = new Configuration();
        config.addEventType("GoalShotEvent", ShotEvent.class.getName());


        String createBallWindow = "create window LastBallEvent.win:length(1) as BallEvent";
        admin.createEPL(createBallWindow);

        String insertBallEvents = "insert into LastBallEvent " +
                                  "select * " +
                                  "from BallEvent " +
                                  "where sid = '4' or sid = '8' or sid = '10' or sid = '12'";

        admin.createEPL(insertBallEvents);


        String createPlayersWindow = "create window Players.std:unique(sid) as SensorEvent";

        admin.createEPL(createPlayersWindow);

        String insertPlayers = "insert into Players " +
                               "select * from SensorEvent " + 
                               "where sid NOT IN ('4', '8', '10', '12', '105', '106')";

        admin.createEPL(insertPlayers);
        
        String detectAndPredictShot =   "select b.sid as sid, b.ts as ts, p.player_id as player_id, b.x as x, b.y as y, b.z as z, b.v as v, b.vx as vx, b.vy as vy, b.vz as vz, b.a as a, b.ax as ax, b.ay as ay, b.az as az, p.team_id as team_id " +
                                        "from Players p, LastBallEvent b " +
                                        "where (p.x - b.x) * (p.x - b.x) + (p.y - b.y) * (p.y - b.y) + (p.z - b.z) * (p.z - b.z) <= 1000000 " +  
                                        "and b.a >= 55000000 " + 
                                        "and p.ts < b.ts " +
                                        "and ( " +
                                        "    (p.team_id = 'teamA' and " +  
                                        "     (b.x + (b.vx * 1.5 / 1000)) between 22560.0 and 29880.0 " +  
                                        "     and (b.y + (b.vy * 1.5 / 1000)) between -31468.0 and -33968.0) " + 
                                        "    or " +
                                        "    (p.team_id = 'teamB' and " +  
                                        "     (b.x + (b.vx * 1.5 / 1000)) between 22578.5 and 29898.5 " +  
                                        "     and (b.y + (b.vy * 1.5 / 1000)) between 31441 and 33941.0) " +  
                                        ")";

        EPStatement statement = admin.createEPL(detectAndPredictShot);

        statement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    //send event
                    GoalShotEvent event1 = new GoalShotEvent(
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
                }
            }
        });

    }
}
