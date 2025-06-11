package com.example;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.RunningStatistics;
import com.espertech.esper.client.*;

public class EventSender {

    public static final Map<String, RunningStatistics> RunningStatisticsMap = new ConcurrentHashMap<>();
    public static final Map<String, SensorEvent> PlayerPosition = new ConcurrentHashMap<>();
    public static final Map<String, String> PlayerIntensity = new ConcurrentHashMap<>();
    public static SensorEvent BallPosition = new SensorEvent("0", 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    public static Set<String> ballIds = Set.of("4", "8", "10", "12");

    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.addEventType("SensorEvent", SensorEvent.class.getName());
        config.addEventType("BallEvent", BallEvent.class.getName());
        config.addEventType("ShotEvent", ShotEvent.class.getName());
        config.addEventType("PlayerPosession", PlayerPosession.class.getName());

        EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(config);

        EPLProcessor processor = new EPLProcessor(epService.getEPAdministrator());
        processor.startListening(epService);

        epService = UDFRegistration.registerUDF();

        new Query3(epService.getEPAdministrator()).startListening(epService);
        new Query4(epService.getEPAdministrator()).startListening(epService);
        new Query4_2(epService.getEPAdministrator()).startListening(epService);
        new Query2(epService.getEPAdministrator()).startListening(epService);
        new ThresholdCalculator(epService.getEPAdministrator()).startListening(epService);
        //new prova(epService.getEPAdministrator()).startListening(epService);


        EPRuntime runtime = epService.getEPRuntime();

        Map<String, PlayerData> metadata = readMetadata("metadata.txt");

        Map<String, String> sidToPlayer = new HashMap<>();
        Map<String, String> sidToTeam = new HashMap<>();
        for (PlayerData player : metadata.values()) {
            String playerId = player.getPlayerName();
            RunningStatisticsMap.put(playerId, new RunningStatistics(0, playerId, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            SensorEvent se = new SensorEvent("0", 0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
            se.setPlayer_id(playerId);
            PlayerPosition.put(playerId, se);
            for (Integer sid : player.getSensorIds()) {
                sidToPlayer.put(String.valueOf(sid), player.getPlayerName());
                sidToTeam.put(String.valueOf(sid), player.getTeamName());
            }
        }

        List<SensorEvent> sensorEvents = streamSensorData("data1.txt", sidToPlayer, sidToTeam);
        sendAllSensorEventsSingleThread(sensorEvents, runtime);
    }

    private static Map<String, PlayerData> readMetadata(String filePath) {
        Map<String, PlayerData> metadata = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String team = "";
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("teamA") || line.startsWith("teamB")) {
                    team = line.split(":")[0].trim();
                } else if (line.contains(",")) {
                    String[] parts = line.split(",");
                    String playerName = parts[0].trim();
                    if (playerName.equalsIgnoreCase("ball: 4") || playerName.toLowerCase().contains("referee"))
                        continue;
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

    private static List<SensorEvent> streamSensorData(String filePath, Map<String, String> sidToPlayer, Map<String, String> sidToTeam) {
        List<SensorEvent> sensorEvents = new LinkedList<>();
        long totalStartTime = System.nanoTime();
        long windowStartTime = System.nanoTime();
        int linesReadInWindow = 0;
        int totalLines = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath), 128 * 1024)) {
            String line;
            StringTokenizer tokenizer;
            while ((line = reader.readLine()) != null) {
                totalLines++;
                linesReadInWindow++;

                tokenizer = new StringTokenizer(line, ",");
                String sid = tokenizer.nextToken();
                long ts = Long.parseLong(tokenizer.nextToken());
                double x = Double.parseDouble(tokenizer.nextToken());
                double y = Double.parseDouble(tokenizer.nextToken());
                double z = Double.parseDouble(tokenizer.nextToken());
                double v = Double.parseDouble(tokenizer.nextToken());
                double a = Double.parseDouble(tokenizer.nextToken());
                double vx = Double.parseDouble(tokenizer.nextToken());
                double vy = Double.parseDouble(tokenizer.nextToken());
                double vz = Double.parseDouble(tokenizer.nextToken());
                double ax = Double.parseDouble(tokenizer.nextToken());
                double ay = Double.parseDouble(tokenizer.nextToken());
                double az = Double.parseDouble(tokenizer.nextToken());

                if (!sid.equals("105") && !sid.equals("106") && !ballIds.contains(sid)) {
                    SensorEvent event = new SensorEvent(sid, ts, x, y, z, v, a, vx, vy, vz, ax, ay, az);
                    event.setPlayer_id(sidToPlayer.get(sid));
                    event.setTeam_id(sidToTeam.get(sid));
                    event.setintensity(determineIntensity(v));
                    //System.out.println("sid: " + sid + " TS: " + ts + " VELOCIDAD: " + v + " INTENSITY: " + event.getintensity());
                    sensorEvents.add(event);
                    PlayerPosition.put(event.getPlayer_id(), event);
                } else if (ballIds.contains(sid)) {
                    SensorEvent event = new SensorEvent(sid, ts, x, y, z, v, a, vx, vy, vz, ax, ay, az);
                    sensorEvents.add(event);
                }

                long now = System.nanoTime();
                if ((now - windowStartTime) >= 1_000_000_000L) {
                    //System.out.println("Lines read in last second: " + linesReadInWindow);
                    linesReadInWindow = 0;
                    windowStartTime = now;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        long totalEndTime = System.nanoTime();
        double duration = (totalEndTime - totalStartTime) / 1e9;
        System.out.println("----------------------------");
        System.out.printf("Total Lines Read: %d\n", totalLines);
        System.out.printf("Total Duration: %.6f seconds\n", duration);
        System.out.printf("Average Speed: %.2f lines/second\n", totalLines / duration);
        System.out.println("----------------------------");

        return sensorEvents;
    }

    private static void sendAllSensorEventsSingleThread(List<SensorEvent> events, EPRuntime runtime) {

        System.out.printf("Sending %d events using a single thread...\n", events.size());
        long startTime = System.nanoTime();
        
        for (SensorEvent event : events) {
            if (ballIds.contains(event.getSid())) {
                BallEvent ball = new BallEvent(event.getSid(), event.getTs(), event.getX(), event.getY(), event.getZ(), event.getV(), event.getA(), event.getVx(), event.getVy(), event.getVz(), event.getAx(), event.getAy(), event.getAz());
                runtime.sendEvent(ball);
            }
            else {
                String playerId = event.getPlayer_id();
                String intensity = event.getintensity();
                
                if (PlayerIntensity.containsKey(playerId)) {
                    String pastIntensity = PlayerIntensity.get(playerId);
                    //resend event if the intensity changes
                    if (pastIntensity != intensity) {
                        //System.out.println("RESEND EVENT!");
                        runtime.sendEvent(event);
                    }
                }

                PlayerIntensity.put(playerId, intensity);
                
                //System.out.println("EVENT SEND! SID: " + event.getSid() + " PLAYER_ID: " + event.getPlayer_id() + " INTENSITY: " + event.getintensity() + " TS: " + event.getTs());
                runtime.sendEvent(event);
            }
        }

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1e9;
        System.out.printf("Sent %d SensorEvents in %.3f seconds (%.2f events/sec)%n",
                events.size(), duration, events.size() / duration);
    }

    private static boolean inside_court(double x, double y) {
        return x >= 0 && x <= 33941 && y <= 33965 && y >= -33960;
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