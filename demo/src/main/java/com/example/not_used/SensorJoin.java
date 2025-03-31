package com.example.not_used;

public class SensorJoin {
    
}
/*package com.example;

import java.io.*;
import java.util.*;

import com.espertech.esper.client.*;
import com.example.SensorEvent;

public class SensorJoin {

       public static void main(String[] args) {
        // Configure Esper
        Configuration config = new Configuration();
        config.addEventType("SensorEvent", SensorEvent.class.getName());
        EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(config);

        Query1 query = new Query1(epService.getEPAdministrator());
        query.startListening();

        EPRuntime runtime = epService.getEPRuntime();

        // EPL Query for Streaming Join
        String eplQuery = "select sid, ts, player_id, team_id, intensity " +
                          "from SensorEvent";

        EPStatement statement = epService.getEPAdministrator().createEPL(eplQuery);

        // Add a listener to print the output in real-time
        statement.addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                if (newEvents != null) {
                    for (EventBean event : newEvents) {
                        System.out.println("Sensor ID: " + event.get("sid") +
                                           ", Timestamp: " + event.get("ts") +
                                           ", Player: " + event.get("player_id") +
                                           ", Team: " + event.get("team_id") +
                                           ", Intensity: " + event.get("intensity"));
                    }
                }
            }
        });

        // Read metadata
        Map<String, PlayerData> metadata = readMetadata("metadata.txt");

        // Stream sensor data
        streamSensorData("data1.txt", metadata, runtime);

        
    }

    private static Map<String, PlayerData> readMetadata(String filePath) {
        Map<String, PlayerData> metadata = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String team = "";
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("teamA") || line.startsWith("teamB")) {
                    team = line.split(":")[0].trim();  // Capture the team name
                } else if (line.contains(",")) {
                    String[] parts = line.split(",");
                    String playerName = parts[0].trim();
                    List<Integer> sensorIds = new ArrayList<>();
                    for (int i = 1; i < parts.length; i++) {
                        sensorIds.add(Integer.parseInt(parts[i].trim()));
                    }
                    metadata.put(playerName, new PlayerData(playerName, team, sensorIds));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return metadata;
    }

    private static void streamSensorData(String filePath, Map<String, PlayerData> metadata, EPRuntime runtime) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String sid = parts[0].trim();
                long ts = Long.parseLong(parts[1].trim());
                double x = Double.parseDouble(parts[2].trim());
                double y = Double.parseDouble(parts[3].trim());
                double z = Double.parseDouble(parts[4].trim());
                double v = Double.parseDouble(parts[5].trim());
                double a = Double.parseDouble(parts[6].trim());
                double vx = Double.parseDouble(parts[7].trim());
                double vy = Double.parseDouble(parts[8].trim());
                double vz = Double.parseDouble(parts[9].trim());
                double ax = Double.parseDouble(parts[10].trim());
                double ay = Double.parseDouble(parts[11].trim());
                double az = Double.parseDouble(parts[12].trim());

                SensorEvent event = new SensorEvent(sid, ts, x, y, z, v, a, vx, vy, vz, ax, ay, az);

                // Assign player and team based on sid
                for (Map.Entry<String, PlayerData> entry : metadata.entrySet()) {
                    PlayerData playerData = entry.getValue();
                    if (playerData.getSensorIds().contains(Integer.parseInt(sid))) {
                        event.setPlayer_id(playerData.getPlayerName());
                        event.setTeam_id(playerData.getTeamName());
                        break;  // Stop looping once found
                    }
                }

                // Set intensity based on speed (v)
                event.setintensity(determineIntensity(v));
                
                Configuration config = new Configuration();
                config.addEventType("SensorEvent", SensorEvent.class);
                EPServiceProvider epService = EPServiceProviderManager.getProvider("MyEsperEngine", config);
                
                // Send event to Esper as a **stream**
                epService.getEPRuntime().sendEvent(event);

                //System.out.println("Event sent: " + event.getSid() + ", " + event.getTs() + ", " + event.getPlayer_id() + ", " + event.getTeam_id() + ", " + event.getintensity());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String determineIntensity(double v) {
        if (v <= 277778) return "standing";
        else if (v <= 3055558) return "trot";
        else if (v <= 3888892) return "low_speed_run";
        else if (v <= 4722226) return "medium_speed_run";
        else if (v <= 5555560) return "high_speed_run";
        else return "sprint";
    }
}
*/