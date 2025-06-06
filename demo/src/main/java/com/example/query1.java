    package com.example;

    import com.RunningStatistics;
    import com.espertech.esper.client.*;
    import com.example.RunningStatisticsEvent;

    import java.util.concurrent.BlockingQueue;

    public class query1 { 
        private final EPAdministrator admin;
        private final BlockingQueue<String> logQueue;

        public query1(EPAdministrator admin, BlockingQueue<String> logQueue) {
            this.admin = admin;
            this.logQueue = logQueue;
        }

        public void startListening(EPServiceProvider epService) {
            epService.getEPAdministrator().getConfiguration().addEventType(RunningStatisticsEvent.class);
            
            String contextEPL = "create context PerPlayerContext partition by player_id from RunningStatisticsEvent";
            admin.createEPL(contextEPL);

            String eplQuery = "context PerPlayerContext " +
                            "select prev(1, ts_start) as prev_ts_start, ts_start, player_id, prev(1, intensity) as prev_intensity, intensity, prev(1, speed) as prev_speed " +
                            "from RunningStatisticsEvent.win:length(2) ";

            EPStatement statement = epService.getEPAdministrator().createEPL(eplQuery);

            statement.addListener((newData, oldData) -> {
                if (newData != null) {
                    for (EventBean event : newData) {
                        Object prevTsStart = event.get("prev_ts_start");
                        Object currTsStart = event.get("ts_start");
                        //System.out.println("PREV: " + prevTsStart + " INTENSITY: " + event.get("prev_intensity") +" CURRENT: " + currTsStart +  " INTENSITY:" + event.get("intensity"));

                        if (prevTsStart != null && currTsStart != null) {
                            double speed = (double) event.get("prev_speed");
                            double distance = ((long) currTsStart - (long) prevTsStart) * speed * 0.000000000000001;
                            String prev_intensity = (String) event.get("prev_intensity");
                            String intensity = (String) event.get("intensity");
                            String playerId = (String) event.get("player_id");

                            RunningStatistics stats = EventSender.RunningStatisticsMap.get(playerId);
                            long time = 0;
                            double total_distance = 0;

                            switch (prev_intensity) {
                                case "standing":
                                    time = stats.getStanding_time();
                                    total_distance = stats.getStanding_distance();
                                    stats.setStanding_time(time + (long)(((long) currTsStart - (long) prevTsStart) * 0.000000001));
                                    stats.setStanding_distance(total_distance + distance);
                                    break;

                                case "trot":
                                    time = stats.getTrot_time();
                                    total_distance = stats.getTrot_distance();
                                    stats.setTrot_time(time + (long)(((long) currTsStart - (long) prevTsStart) * 0.000000001));
                                    stats.setTrot_distance(distance + total_distance);
                                    break;

                                case "low_speed_run":
                                    time = stats.getLow_time();
                                    total_distance = stats.getLow_distance();
                                    stats.setLow_time(time + (long)(((long) currTsStart - (long) prevTsStart) * 0.000000001));
                                    stats.setLow_distance(distance + total_distance);
                                    break;

                                case "medium_speed_run":
                                    time = stats.getMedium_time();
                                    total_distance = stats.getMedium_distance();
                                    stats.setMedium_time(time + (long)(((long) currTsStart - (long) prevTsStart) * 0.000000001));
                                    stats.setMedium_distance(distance + total_distance);
                                    break;

                                case "high_speed_run":
                                    time = stats.getHigh_time();
                                    total_distance = stats.getHigh_distance();
                                    stats.setHigh_time(time + (long)(((long) currTsStart - (long) prevTsStart) * 0.000000001));
                                    stats.setHigh_distance(distance + total_distance);
                                    break;

                                case "sprint":
                                    time = stats.getSprint_time();
                                    total_distance = stats.getSprint_distance();
                                    stats.setSprint_time(time + (long)(((long) currTsStart - (long) prevTsStart) * 0.000000001));
                                    stats.setSprint_distance(distance + total_distance);
                                    break;

                                default:
                                    break;
                            }
                            if (intensity != prev_intensity) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("Run Segment:\n")
                                .append("-----------------------------\n")
                                .append("Start:      ").append(prevTsStart).append("\n")
                                .append("Stop:       ").append(currTsStart).append("\n")
                                .append("Player:     ").append(playerId).append("\n")
                                .append("Intensity:  ").append(prev_intensity).append("\n")
                                .append("Distance:   ").append(distance).append(" mm\n")
                                .append("Avg Speed:  ").append(speed).append(" μm/s\n\n");

                                sb.append("Updated Running Statistics for Player ").append(playerId).append(":\n")
                                .append("-----------------------------------------------------\n")
                                .append("Standing Time:        ").append(stats.getStanding_time()).append(" milliseconds\n")
                                .append("Standing Distance:    ").append(stats.getStanding_distance()).append(" mm\n")
                                .append("Trot Time:            ").append(stats.getTrot_time()).append(" milliseconds\n")
                                .append("Trot Distance:        ").append(stats.getTrot_distance()).append(" mm\n")
                                .append("Low Speed Run Time:   ").append(stats.getLow_time()).append(" milliseconds\n")
                                .append("Low Speed Run Dist.:  ").append(stats.getLow_distance()).append(" mm\n")
                                .append("Medium Speed Run Time:").append(stats.getMedium_time()).append(" milliseconds\n")
                                .append("Medium Speed Run Dist:").append(stats.getMedium_distance()).append(" mm\n")
                                .append("High Speed Run Time:  ").append(stats.getHigh_time()).append(" milliseconds\n")
                                .append("High Speed Run Dist.: ").append(stats.getHigh_distance()).append(" mm\n")
                                .append("Sprint Time:          ").append(stats.getSprint_time()).append(" milliseconds\n")
                                .append("Sprint Distance:      ").append(stats.getSprint_distance()).append(" mm\n\n");

                                

                                logQueue.offer(sb.toString());

                            } else {
                                //logQueue.offer("Previous event is null (only one event in window)");
                            }
                            }
                    }
                }
            });
        }
    }
