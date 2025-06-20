package com.example;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;

public class prova {
    private EPAdministrator admin;
    private final AsyncLogger logger;
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();


    public prova(EPAdministrator admin) {
        this.admin = admin;
        this.logger = new AsyncLogger("prova.txt", logQueue);
    }

    public void startListening(EPServiceProvider epService) {
      
        String eplQuery =   "select * " +
                            "from SensorEvent ";


        EPStatement statement = admin.createEPL(eplQuery);

        statement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    String playerId = (String) event.get("player_id");
                    Object ts = event.get("ts");
                    Object sid = event.get("sid");
                    Object x = event.get("x");
                    Object y = event.get("y");

                    StringBuilder sb = new StringBuilder();
                    sb.append(sid).append(",").append(ts).append(",").append(playerId).append(",").append(x).append(",").append(y).append("\n");
                    logQueue.offer(sb.toString());
                }
            }
        });

        /*String eplQuery1 =   "select * " +
                            "from BallEvent ";


        EPStatement statement1 = admin.createEPL(eplQuery1);

        statement1.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    //String playerId = (String) event.get("player_id");
                    Object ts = event.get("ts");
                    Object sid = event.get("sid");

                    StringBuilder sb = new StringBuilder();
                    sb.append(sid).append(",").append(ts).append("\n");
                    logQueue.offer(sb.toString());
                }
            }
        });*/
    }
}
