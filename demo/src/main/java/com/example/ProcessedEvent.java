package com.example;

public class ProcessedEvent {
    private String player_id;
    private String intensity;
    private long duration;
    private double distance;

    public ProcessedEvent(String player_id, String intensity, long duration, double distance) {
        this.player_id = player_id;
        this.intensity = intensity;
        this.duration = duration;
        this.distance = distance;
    }

    public String getPlayer_id() { return player_id; }
    public String getIntensity() { return intensity; }
    public long getDuration() { return duration; }
    public double getDistance() { return distance; }
    
}
