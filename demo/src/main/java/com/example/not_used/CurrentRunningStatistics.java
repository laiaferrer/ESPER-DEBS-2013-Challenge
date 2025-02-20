package com.example.not_used;

public class CurrentRunningStatistics {
    private long ts_start;
    private long ts_stop;
    private String player_id;
    private String intensity;
    private double distance;
    private double speed;

    public CurrentRunningStatistics(long ts_start, long ts_stop, String player_id, String intensity,
                                    double distance, double speed) {
        this.ts_start = ts_start;
        this.ts_stop = ts_stop;
        this.player_id = player_id;
        this.intensity = intensity;
        this.distance = distance;
        this.speed = speed;
    }

    public long getTs_start() {
        return ts_start;
    }
    
    public long getTs_stop() {
        return ts_stop;
    }
    
    public String getPlayer_id() {
        return player_id;
    }
    
    public String getIntensity() {
        return intensity;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public double getSpeed() {
        return speed;
    }
}
