package com.example;

public class HeatMapEvent {
    private long ts;
    private String player_id;
    private double cell_x;
    private double cell_y;
    private long duration;

    // Constructor
    public HeatMapEvent(long ts, String player_id, double cell_x, double cell_y, long duration) {
        this.ts = ts;
        this.player_id = player_id;
        this.cell_x = cell_x;
        this.cell_y = cell_y;
        this.duration = duration;
    }

    // Getters
    public long getTs() {
        return ts;
    }

    public String getPlayerId() {
        return player_id;
    }

    public double getCellX() {
        return cell_x;
    }

    public double getCellY() {
        return cell_y;
    }

    public long getDuration() {
        return duration;
    }

    // Setters
    public void setTs(long ts) {
        this.ts = ts;
    }

    public void setPlayerId(String player_id) {
        this.player_id = player_id;
    }

    public void setCellX(double cell_x) {
        this.cell_x = cell_x;
    }

    public void setCellY(double cell_y) {
        this.cell_y = cell_y;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
