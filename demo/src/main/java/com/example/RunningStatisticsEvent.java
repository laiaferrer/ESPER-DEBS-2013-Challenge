package com.example;

public class RunningStatisticsEvent {
    private long ts_start;
    private long ts_stop;
    private String player_id;
    private String intensity;
    private double speed;

    // Constructor with all fields
    public RunningStatisticsEvent(long ts_start, long ts_stop, String player_id, String intensity, double speed) {
        this.ts_start = ts_start;
        this.ts_stop = ts_stop;
        this.player_id = player_id;
        this.intensity = intensity;
        this.speed = speed;
    }

    // Default constructor
    public RunningStatisticsEvent() {}

    // Getters and Setters
    public long getTs_start() {
        return ts_start;
    }

    public void setTs_start(long ts_start) {
        this.ts_start = ts_start;
    }

    public long getTs_stop() {
        return ts_stop;
    }

    public void setTs_stop(long ts_stop) {
        this.ts_stop = ts_stop;
    }

    public String getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(String player_id) {
        this.player_id = player_id;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
