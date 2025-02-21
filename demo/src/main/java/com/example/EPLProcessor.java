package com.example;

import com.RunningStatistics;
import com.espertech.esper.client.*;
import com.example.EventSender;

//this code solves Query 1

public class EPLProcessor {
    private EPAdministrator admin;

    public EPLProcessor(EPAdministrator admin) {
        this.admin = admin;
    }

    public void startListening(EPServiceProvider epService) {


        String contextEPL = "create context IntensityContext " +
                            "partition by player_id from SensorEvent " +
                            "initiated by SensorEvent as a " +
                            "terminated by SensorEvent(intensity != a.intensity)";

            
        /*String eplQuery = "select " +
                            "  a.ts as ts_start, " +
                            "  b.ts as ts_stop, " +
                            "  a.player_id, " +
                            "  a.intensity, " +
                            "  ((b.ts - a.ts) * avg(a.v)) * 1e-15 as distance, " +
                            "  avg(a.v) as speed " +
                            "from pattern [" +
                            "   a=SensorEvent() -> every b=SensorEvent(a.player_id = b.player_id and a.intensity != b.intensity)" +
                            "]";*/
        
        
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
                          "output snapshot when terminated";
          
        
        epService.getEPAdministrator().createEPL(contextEPL);

        EPStatement statement = admin.createEPL(eplQuery);
                
        statement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    //System.out.println("Run Segment: ");
                    
                    System.out.printf(
                        "Run Segment:%n" +
                        "-----------------------------%n" +
                        "Start:      %s%n" +
                        "Stop:       %s%n" +
                        "Player:     %s%n" +
                        "Intensity:  %s%n" +
                        "Distance:   %s mm%n" +
                        "Avg Speed:  %s μm/s%n%n",
                        event.get("ts_start"),
                        event.get("ts_stop"),
                        event.get("player_id"),
                        event.get("intensity"),
                        event.get("distance"),
                        event.get("speed")
                    );

                    RunningStatistics stats = EventSender.RunningStatisticsMap.get(event.get("player_id"));
                    String intensity = (String) event.get("intensity");  
                    long time;
                    double distance;
                    //System.out.println("hola: " + intensity);
                    switch (intensity) {
                        case "standing":
                            time = stats.getStanding_time();
                            distance = stats.getStanding_distance();
                            stats.setStanding_time(time + ((long) event.get("ts_stop") - (long) event.get("ts_start")));
                            stats.setStanding_distance(distance + (double) event.get("distance"));
                            break;
    
                        case "trot":
                            time = stats.getTrot_time();
                            distance = stats.getTrot_distance();
                            stats.setTrot_time(time + ((long) event.get("ts_stop") - (long) event.get("ts_start")));
                            stats.setTrot_distance(distance + (double) event.get("distance"));
                            break;
    
                        case "low_speed_run":
                            time = stats.getLow_time();
                            distance = stats.getLow_distance();
                            stats.setLow_time(time + ((long) event.get("ts_stop") - (long) event.get("ts_start")));
                            stats.setLow_distance(distance + (double) event.get("distance"));
                            break;
    
                        case "medium_speed_run":
                            time = stats.getMedium_time();
                            distance = stats.getMedium_distance();
                            stats.setMedium_time(time + ((long) event.get("ts_stop") - (long) event.get("ts_start")));
                            stats.setMedium_distance(distance + (double) event.get("distance"));
                            break;
    
                        case "high_speed_run":
                            time = stats.getHigh_time();
                            distance = stats.getHigh_distance();
                            stats.setHigh_time(time + ((long) event.get("ts_stop") - (long) event.get("ts_start")));
                            stats.setHigh_distance(distance + (double) event.get("distance"));
                            break;
    
                        case "sprint":
                            time = stats.getSprint_time();
                            distance = stats.getSprint_distance();
                            stats.setSprint_time(time + ((long) event.get("ts_stop") - (long) event.get("ts_start")));
                            stats.setSprint_distance(distance + (double) event.get("distance"));
                            break;
    
                        default:
                            break;
                    }

                    System.out.printf(
                        "Updated Running Statistics for Player %s:%n" +
                        "-----------------------------------------------------%n" +
                        "Standing Time:        %s picoseconds%n" +
                        "Standing Distance:    %s mm%n" +
                        "Trot Time:           %s picoseconds%n" +
                        "Trot Distance:       %s mm%n" +
                        "Low Speed Run Time:   %s picoseconds%n" +
                        "Low Speed Run Dist.:  %s mm%n" +
                        "Medium Speed Run Time:%s picoseconds%n" +
                        "Medium Speed Run Dist:%s mm%n" +
                        "High Speed Run Time:  %s picoseconds%n" +
                        "High Speed Run Dist.: %s mm%n" +
                        "Sprint Time:         %s picoseconds%n" +
                        "Sprint Distance:     %s mm%n%n",
                        stats.getPlayer_id(),
                        stats.getStanding_time(), stats.getStanding_distance(),
                        stats.getTrot_time(), stats.getTrot_distance(),
                        stats.getLow_time(), stats.getLow_distance(),
                        stats.getMedium_time(), stats.getMedium_distance(),
                        stats.getHigh_time(), stats.getHigh_distance(),
                        stats.getSprint_time(), stats.getSprint_distance()
                    );

                    
                }
            }
        });

        System.out.println("Esper Query is runningggg...");
    }
}
