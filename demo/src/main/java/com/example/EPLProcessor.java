package com.example;

import com.RunningStatistics;
import com.espertech.esper.client.*;
import com.example.EventSender;
import com.example.RunningStatisticsEvent;

//this code solves Query 1

public class EPLProcessor {
    private EPAdministrator admin;

    public EPLProcessor(EPAdministrator admin) {
        this.admin = admin;
    }

    public void startListening(EPServiceProvider epService) {

        // Register the listener
        query1 processor = new query1(epService.getEPAdministrator());
        processor.startListening(epService);

        Configuration config = new Configuration();
        // Register the event type (RunningStatisticsEvent)
        config.addEventType("RunningStatisticsEvent", RunningStatisticsEvent.class.getName());
            
        String contextEPL = "create context IntensityContext " +
                            "partition by player_id from SensorEvent " +
                            "initiated by SensorEvent(sid NOT IN ('4', '8', '10', '12')) as a " +
                            "terminated by SensorEvent(intensity != a.intensity AND (ts - a.ts) >= 1000000000000) ";

        
        String eplQuery = "context IntensityContext " +
                          "select " +
                          "  min(ts) as ts_start, " +
                          "  max(ts) as ts_stop, " +
                          "  player_id, " +
                          "  intensity, " +
                          "  ((max(ts) - min(ts)) * avg(v)) * 0.000000000000001 as distance, " +
                          "  avg(v) as speed " +
                          "from SensorEvent " +
                          "group by player_id, intensity " +
                          "output last every 20 milliseconds";

          
        
        epService.getEPAdministrator().createEPL(contextEPL);

        EPStatement statement = admin.createEPL(eplQuery);
                
        statement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    //send event
                    RunningStatisticsEvent event1 = new RunningStatisticsEvent((long) event.get("ts_start"), (String) event.get("player_id"), (String) event.get("intensity"), (double) event.get("speed"));
                    //System.out.println("Sending RunningStatisticsEvent: " + event1);
                    epService.getEPRuntime().sendEvent(event1);
                }
            }
        });

        System.out.println("Esper Query is runningggg...");
    }
}
