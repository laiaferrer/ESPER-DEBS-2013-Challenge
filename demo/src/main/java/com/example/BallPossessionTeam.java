package com.example;

public class BallPossessionTeam {
    private long ts;          // Timestamp
    private String teamId;     // Player ID
    private double time;      // Time
    private double timePercent;         // Hits

    public BallPossessionTeam(long ts, String teamId, double time, double timePercent) {
        this.ts = ts;
        this.teamId = teamId;
        this.time = time;
        this.timePercent = timePercent;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getTimePercent() {
        return timePercent;
    }

    public void setTimePercent(double timePercent) {
        this.timePercent = timePercent;
    }

}
