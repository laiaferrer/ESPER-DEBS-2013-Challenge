package com.example;

public class SensorEvent {

    private String sid;
    private long ts;
    private double x;
    private double y;
    private double z;
    private double v;  // |v|
    private double a;  // |a|
    private double vx;
    private double vy;
    private double vz;
    private double ax;
    private double ay;
    private double az;
    private String player_id;
    private String team_id;
    private String intensity;

    // Constructor to initialize all fields
    public SensorEvent(String sid, long ts, double x, double y, double z, double v, double a,
                        double vx, double vy, double vz, double ax, double ay, double az) {
        this.sid = sid;
        this.ts = ts;
        this.x = x;
        this.y = y;
        this.z = z;
        this.v = v;
        this.a = a;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.ax = ax;
        this.ay = ay;
        this.az = az;
        this.player_id = "";
        this.team_id = "";
        this.intensity = "";
    }

    // Getters for each field
    public String getSid() {
        return sid;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getV() {
        return v;
    }

    public double getA() {
        return a;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getVz() {
        return vz;
    }

    public double getAx() {
        return ax;
    }

    public double getAy() {
        return ay;
    }

    public double getAz() {
        return az;
    }

    public String getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(String player_id) {
        this.player_id = player_id;
    }

    public String getTeam_id() {
        return team_id;
    }

    public void setTeam_id(String team_id) {
        this.team_id = team_id;
    }

    public String getintensity() {
        return intensity;
    }

    public void setintensity(String intensity) {
        this.intensity = intensity;
    }
    
}

