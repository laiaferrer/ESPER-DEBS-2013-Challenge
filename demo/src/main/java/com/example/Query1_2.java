    package com.example;

    import com.RunningStatistics;
    import com.espertech.esper.client.EPAdministrator;
    import com.espertech.esper.client.EPServiceProvider;
    import com.espertech.esper.client.EPStatement;
    import com.espertech.esper.client.EventBean;

    import com.example.EventSender;  

    public class Query1_2 {
        private EPAdministrator admin;

        public Query1_2(EPAdministrator admin) {
            this.admin = admin;
        }  
        
        public void startListening(EPServiceProvider epService) {
            String eplQuery = "select *" +
                            "from ProcessedEvent ";
            
            EPStatement statement = admin.createEPL(eplQuery);

            /*statement.addListener((newData, oldData) -> {
                if (newData != null) {
                    for (EventBean event : newData) {
                        //System.out.println("Run Segment: ");

                        RunningStatistics stats = EventSender.RunningStatisticsMap.get(event.get("player_id"));

                        String intensity = (String) event.get("intensity");  
                        long time;
                        double distance;

                        switch (intensity) {
                            case "standing":
                                time = stats.getStanding_time();
                                distance = stats.getStanding_distance();
                                stats.setStanding_time(time + (long) event.get("duration"));
                                stats.setStanding_distance(distance + (double) event.get("distance"));
                                break;
        
                            case "trot":
                                time = stats.getTrot_time();
                                distance = stats.getTrot_distance();
                                stats.setTrot_time(time + (long) event.get("duration"));
                                stats.setTrot_distance(distance + (double) event.get("distance"));
                                break;
        
                            case "low_speed_run":
                                time = stats.getLow_time();
                                distance = stats.getLow_distance();
                                stats.setLow_time(time + (long) event.get("duration"));
                                stats.setLow_distance(distance + (double) event.get("distance"));
                                break;
        
                            case "medium_speed_run":
                                time = stats.getMedium_time();
                                distance = stats.getMedium_distance();
                                stats.setMedium_time(time + (long) event.get("duration"));
                                stats.setMedium_distance(distance + (double) event.get("distance"));
                                break;
        
                            case "high_speed_run":
                                time = stats.getHigh_time();
                                distance = stats.getHigh_distance();
                                stats.setHigh_time(time + (long) event.get("duration"));
                                stats.setHigh_distance(distance + (double) event.get("distance"));
                                break;
        
                            case "sprint":
                                time = stats.getSprint_time();
                                distance = stats.getSprint_distance();
                                stats.setSprint_time(time + (long) event.get("duration"));
                                stats.setSprint_distance(distance + (double) event.get("distance"));
                                break;
        
                            default:
                                break;
                        }
                        
                        System.out.println("Received Processed Event: " +
                                "Player: " + event.get("player_id") + ", " +
                                "Intensity: " + event.get("intensity") + ", " +
                                "Duration: " + event.get("duration") + " ms, " +
                                "Distance: " + event.get("distance") + " mm");
                        
                    }
                }
            }); */
        }
    }
