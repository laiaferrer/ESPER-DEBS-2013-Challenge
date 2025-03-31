package com.example.not_used;

public class Query1 {
    
}

/*package com.example;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;


public class Query1 { 
    private EPAdministrator admin;

    public void run() {
        // Configure Esper
        Configuration config = new Configuration();
        config.addEventType("SensorEvent", SensorEvent.class);
        EPServiceProvider epService = EPServiceProviderManager.getProvider("MyEsperEngine", config);
        */
        /*
        String contextEPL = "create context IntensityContext " +
                            "partition by player_id from SensorEvent " +
                            "initiated by SensorEvent as a " +
                            "terminated by SensorEvent(intensity != a.intensity and player_id = a.player_id)";
        */
        
        //String eplQuery = /*"context IntensityContext " +*/
                          //"select * " +
                          //"from SensorEvent "
                          //group by player_id, intensity 
                          //;

        // Register EPL statements
        /*
        epService.getEPAdministrator().createEPL(contextEPL);
        */
        /*
        EPStatement statement = epService.getEPAdministrator().createEPL(eplQuery);
        MyListener listener = new MyListener();
        statement.addListener(listener);
        */
        /*
        // Add listener to process query results
        statement.addListener(new UpdateListener() {
            @Override
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                System.out.println("1");
                if (newEvents != null) {
                    for (EventBean event : newEvents) {
                        System.out.println("Run Segment: " +
                                "Start: " + event.get("ts_start") + ", " +
                                "Stop: " + event.get("ts_stop") + ", " +
                                "Player: " + event.get("player_id") + ", " +
                                "Intensity: " + event.get("intensity") + ", " +
                                "Distance: " + event.get("distance") + " m, " +
                                "Avg Speed: " + event.get("speed") + " m/s");
                    }
                }
            }
        });
        */
        //System.out.println("Esper Query is runningggg...");
    //}

    //public class MyListener implements UpdateListener {
      //  public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        //    System.out.println("1");
          //  if (newEvents != null) {
            //    for (EventBean event : newEvents) {
              //      System.out.println("hola");
                    /*System.out.println("Run Segment: " +
                            "Start: " + event.get("ts_start") + ", " +
                            "Stop: " + event.get("ts_stop") + ", " +
                            "Player: " + event.get("player_id") + ", " +
                            "Intensity: " + event.get("intensity") + ", " +
                            "Distance: " + event.get("distance") + " m, " +
                            "Avg Speed: " + event.get("speed") + " m/s");*/
               // }
            //}
       // }
    //}

    /*public static void main(String[] args) {
        SensorJoin.main(new String[]{});
    
        try { 
            Thread.sleep(2000); // Small delay to allow event streaming
        } catch (InterruptedException e) { 
            e.printStackTrace(); 
        }

        Query1 example = new Query1();
        example.run();
    }
}*/
