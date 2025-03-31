package com.example;

public class BallPossessionPlayer {
    private long ts;          // Timestamp
    private String playerId;     // Player ID
    private double time;      // Time
    private int hits;         // Hits

    // Constructor
    public BallPossessionPlayer(long ts, String playerId, double time, int hits) {
        this.ts = ts;
        this.playerId = playerId;
        this.time = time;
        this.hits = hits;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }
}
