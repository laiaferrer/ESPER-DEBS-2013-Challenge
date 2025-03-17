package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.RunningStatistics;
import com.espertech.esper.client.*;


public class EventSender {

    public static final Map<String, RunningStatistics> RunningStatisticsMap = new HashMap<>();
    public static final Map<String, SensorEvent> PlayerPosition = new HashMap<>();
    public static SensorEvent BallPosition = new SensorEvent("0", 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);


    public static void main(String[] args) {
        // Create an Esper Configuration
        Configuration config = new Configuration();
        config.addEventType("SensorEvent", SensorEvent.class.getName());
        config.addEventType("BallEvent", BallEvent.class.getName());
        config.addEventType("ShotEvent", ShotEvent.class.getName());
        config.addEventType("PlayerPosession", PlayerPosession.class.getName());
        
        
        // Create an Esper runtime
        EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(config);

        // Register the listener
        EPLProcessor processor = new EPLProcessor(epService.getEPAdministrator());
        processor.startListening(epService);

        epService = UDFRegistration.registerUDF();

        //Register another listener
        Query3 query3 = new Query3(epService.getEPAdministrator());
        query3.startListening(epService);

        Query4 query4 = new Query4(epService.getEPAdministrator());
        query4.startListening(epService);

        Query4_2 query4_2 = new Query4_2(epService.getEPAdministrator());
        query4_2.startListening(epService);

        Query2 query2 = new Query2(epService.getEPAdministrator());
        query2.startListening(epService);

        // Get an event runtime
        EPRuntime runtime = epService.getEPRuntime();

        // EPL Query for Streaming Join
        String eplQuery = "select sid, ts, player_id, team_id, intensity " +
                          "from SensorEvent";

        EPStatement statement = epService.getEPAdministrator().createEPL(eplQuery);

        statement.addListener(new UpdateListener() {
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                if (newEvents != null) {
                    for (EventBean event : newEvents) {
                        /*System.out.println("Sensor ID: " + event.get("sid") +
                                           ", Timestamp: " + event.get("ts") +
                                           ", Player: " + event.get("player_id") +
                                           ", Team: " + event.get("team_id") +
                                           ", Intensity: " + event.get("intensity"));*/
                    }
                }
            }
        });

        // Read metadata
        Map<String, PlayerData> metadata = readMetadata("metadata.txt");

                
        for (PlayerData player : metadata.values()) {
            String playerId = player.getPlayerName(); // You might have a getter for playerId in PlayerData class
            RunningStatistics stats = new RunningStatistics(0, playerId, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            RunningStatisticsMap.put(playerId, stats);
            //System.out.println("Stored RunningStatistics for player: " + playerId + " -> " + stats);
        }

        for (PlayerData player : metadata.values()) {
            String playerId = player.getPlayerName(); // You might have a getter for playerId in PlayerData class
            SensorEvent stats = new SensorEvent("0", 0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);;
            stats.setPlayer_id(playerId);
            PlayerPosition.put(playerId, stats);
            //System.out.println("Stored PlayerPostition for player: " + playerId + " -> " + stats);
        }
        

        // Stream sensor data
        streamSensorData("data1.txt", metadata, runtime, epService);

        RunningStatisticsMap.clear();
        PlayerPosition.clear();
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

                    if (playerName.equalsIgnoreCase("ball: 4") || playerName.toLowerCase().contains("referee")) {
                        continue;
                    }

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

    private static boolean inside_court (double x, double y) {

        if(x >= 0 && x <= 33941 &&  y <= 33965 && y >= -33960) {
            return true;
        }
        return false;
    }

    private static void streamSensorData(String filePath, Map<String, PlayerData> metadata, EPRuntime runtime, EPServiceProvider epService) {
        
        long startTime = System.nanoTime();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 13) {  // Expecting at least 13 values
                    System.err.println("Malformed line: " + line + " (length: " + parts.length + ")");
                    continue;  // Skip this line
                }
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

                
                if (sid.equals("4") || sid.equals("8") || sid.equals("10") || sid.equals("12")) {
                    
                    if (inside_court(x, y)) {
                        BallEvent event = new BallEvent(sid, ts, x, y, z, v, a, vx, vy, vz, ax, ay, az);
                        //System.out.println("Event sent: " + event.getSid() + ", " + event.getTs() + ", " + " x: " + event.getX() + " y: " + event.getY());
                        
                        // Send event to Esper as a **stream**
                        epService.getEPRuntime().sendEvent(event);
                    }
                }
                else {     
                    if (sid != "105" && sid != "106")  {
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

                        //System.out.println("Event sent: " + event.getSid() + ", " + event.getTs() + ", " + event.getPlayer_id() + ", " + event.getTeam_id() + ", " + event.getintensity() + " x: " + event.getX() + " y: " + event.getY());
                        
                        // Send event to Esper as a **stream**
                        epService.getEPRuntime().sendEvent(event);

                        //if it is the event that will terminate the context resend it so that it can initialize another context
                        String i =  PlayerPosition.get(event.getPlayer_id()).getintensity();
                        //System.out.println("Previous intensity: " + i + " Actual intensity: "+ event.getintensity());
                        if(event.getintensity() != i && i != "") {
                            //System.out.println("Repeated Event sent: " + event.getSid() + ", " + event.getTs() + ", " + event.getPlayer_id() + ", " + event.getTeam_id() + ", " + event.getintensity());
                            //event.setTs(event.getTs() + 1);
                            epService.getEPRuntime().sendEvent(event);
                        }


                        //update the position of the player
                        PlayerPosition.put(event.getPlayer_id(), event);

                        //System.out.println("Event sent: " + event.getSid() + ", " + event.getTs() + ", " + event.getPlayer_id() + ", " + event.getTeam_id() + ", " + event.getintensity());
                    }
                    
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime(); // End measuring time
        double duration = (endTime - startTime) / 1_000_000_000.0; // Convert to sec

        System.out.println("----------------------------");
        System.out.println("File Read Execution Time:");
        System.out.printf("Duration: %.6f seconds%n", duration);
        System.out.println("----------------------------");
    }

    private static String determineIntensity(double v) {
        if (v <= 277778) return "standing";
        else if (v <= 3055555) return "trot";
        else if (v <= 3888889) return "low_speed_run";
        else if (v <= 4722222) return "medium_speed_run";
        else if (v <= 6666667) return "high_speed_run";
        else return "sprint";
    }
}
