package com.example;

import com.RunningStatistics;
import com.espertech.esper.client.*;
import com.example.EventSender;

public class EPLProcessor {
    private EPAdministrator admin;

    public EPLProcessor(EPAdministrator admin) {
        this.admin = admin;
    }

    public void startListening(EPServiceProvider epService) {

        String contextEPL = "create context IntensityContext " +
                            "partition by player_id from SensorEvent " +
                            "initiated by SensorEvent as a " +
                            "terminated by SensorEvent(intensity != a.intensity and player_id = a.player_id)";
        
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
                    
                    System.out.println("Run Segment: " +
                            "Start: " + event.get("ts_start") + ", " +
                            "Stop: " + event.get("ts_stop") + ", " +
                            "Player: " + event.get("player_id") + ", " +
                            "Intensity: " + event.get("intensity") + ", " +
                            "Distance: " + event.get("distance") + " mm, " +
                            "Avg Speed: " + event.get("speed") + " μm/s");

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

                    System.out.println( "Updated Running Statistics for Player " + stats.getPlayer_id() + ": " +
                                        "Standing Time: " + stats.getStanding_time() + " picoseconds, Standing Distance: " + stats.getStanding_distance() + " mm, " +
                                        "Trot Time: " + stats.getTrot_time() + " picoseconds, Trot Distance: " + stats.getTrot_distance() + " mm, " +
                                        "Low Speed Run Time: " + stats.getLow_time() + " picoseconds, Low Speed Run Distance: " + stats.getLow_distance() + " mm, " +
                                        "Medium Speed Run Time: " + stats.getMedium_time() + " picoseconds, Medium Speed Run Distance: " + stats.getMedium_distance() + " mm, " +
                                        "High Speed Run Time: " + stats.getHigh_time() + " picoseconds, High Speed Run Distance: " + stats.getHigh_distance() + " mm, " +
                                        "Sprint Time: " + stats.getSprint_time() + " picoseconds, Sprint Distance: " + stats.getSprint_distance() + " mm");
                    
                }
            }
        });

        System.out.println("Esper Query is runningggg...");
    }
}
