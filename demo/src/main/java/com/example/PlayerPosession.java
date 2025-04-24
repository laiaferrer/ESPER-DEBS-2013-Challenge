package com.example;

public class PlayerPosession {
    private long ts;
    private String playerId;
    private long duration;
    private int hits;
    private String teamId;

    // Constructor
    public PlayerPosession(long ts, String playerId, long duration, int hits, String teamId) {
        this.ts = ts;
        this.playerId = playerId;
        this.duration = duration;
        this.hits = hits;
        this.teamId = teamId;
    }

    // Getters
    public long getTs() {
        return ts;
    }

    public String getPlayerId() {
        return playerId;
    }

    public long getDuration() {
        return duration;
    }

    public int getHits() {
        return hits;
    }

    public String getTeamId() {
        return teamId;
    }

    // Setters
    public void setTs(long ts) {
        this.ts = ts;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
}
