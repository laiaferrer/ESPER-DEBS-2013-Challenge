    package com.example;

    import java.util.concurrent.BlockingQueue;
    import java.util.concurrent.LinkedBlockingQueue;
    
    import com.RunningStatistics;
    import com.espertech.esper.client.*;
    import com.example.EventSender;
    import com.example.RunningStatisticsEvent;
 
    //this code solves Query 1

    public class EPLProcessor {
        private EPAdministrator admin;
        private long eventCount = 0;
        private long startTime = System.currentTimeMillis();
        private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
        private final AsyncLogger logger;



        public EPLProcessor(EPAdministrator admin) {
            this.admin = admin;
            this.logger = new AsyncLogger("query1.txt", logQueue);
        }

        public void startListening(EPServiceProvider epService) {
            
            // Register the listener
            query1 processor = new query1(epService.getEPAdministrator(), logQueue);    
            processor.startListening(epService);

            Configuration config = new Configuration();
            // Register the event type (RunningStatisticsEvent)
            config.addEventType("RunningStatisticsEvent", RunningStatisticsEvent.class.getName());
                
            
            String contextEPL = "create context IntensityContext " +
                                "partition by player_id from SensorEvent " +
                                "initiated by SensorEvent(sid NOT IN ('4', '8', '10', '12', '105', '106')) as a " +
                                "terminated by SensorEvent(a.player_id = player_id and  intensity != a.intensity AND (ts - a.ts) >= 1000000000000)";
            
                        
            String eplQuery = "context IntensityContext " +
                            "select " +
                            "  min(ts) as ts_start, " +
                            "  max(ts) as ts_stop, " +
                            "  player_id, " +
                            "  intensity, " +
                            "  ((max(ts) - min(ts)) * avg(v)) * 0.000000000000001 as distance, " +
                            "  avg(v) as speed " +
                            "from SensorEvent " +
                            "output last when terminated";
            
            EPStatement statement2 = epService.getEPAdministrator().createEPL(contextEPL);
            statement2.addListener((newData, oldData) -> {
                if (newData != null) {
                    for (EventBean event : newData) {
                        //System.out.println("context triggered");

                        //System.out.println("CHANGE OF INTENSITY: "+ event.get("intensity"));
                    }
                }
            });

            EPStatement statement = admin.createEPL(eplQuery);
                    
            statement.addListener((newData, oldData) -> {
                if (newData != null) {
                    for (EventBean event : newData) {
                        //send event
                        RunningStatisticsEvent event1 = new RunningStatisticsEvent((long) event.get("ts_start"), (long) event.get("ts_stop"), (String) event.get("player_id"), (String) event.get("intensity"), (double) event.get("speed"));
                        //System.out.println("Sending RunningStatisticsEvent: PLAYER: " + event1.getPlayer_id() + "INTENSITY: " + event1.getIntensity() + " TS_START: " + event1.getTs_start() + " TS_STOP: " + event1.getTs_stop());
                        epService.getEPRuntime().sendEvent(event1);
                    }
                }
            });

            

            System.out.println("Esper Query is runningggg...");
        }
    }
