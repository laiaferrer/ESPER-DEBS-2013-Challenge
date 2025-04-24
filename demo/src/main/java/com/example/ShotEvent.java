package com.example;

public class ShotEvent {

    private String sid;
    private long ts;
    private String playerId;
    private double x;
    private double y;
    private double z;
    private double v;  // |v|
    private double vx;
    private double vy;
    private double vz;
    private double a;  // |a|
    private double ax;
    private double ay;
    private double az;
    private String team_id;

    // Constructor
    public ShotEvent(String sid, long ts, String player_id, double x, double y, double z, double v, double vx, double vy, double vz,
                     double a, double ax, double ay, double az, String team_id) {
        this.sid = sid;
        this.ts = ts;
        this.playerId = player_id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.v = v;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.a = a;
        this.ax = ax;
        this.ay = ay;
        this.az = az;
        this.team_id = team_id;
    }

    // Getters
    public String getSid() {
        return sid;
    }

    public long getTs() {
        return ts;
    }

    public String getPlayerId() {
        return playerId;
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

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getVz() {
        return vz;
    }

    public double getA() {
        return a;
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

    public String getTeamId() {
        return team_id;
    }

    // Setters
    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public void setPlayerId(String player_id) {
        this.playerId = player_id;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setV(double v) {
        this.v = v;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public void setVz(double vz) {
        this.vz = vz;
    }

    public void setA(double a) {
        this.a = a;
    }

    public void setAx(double ax) {
        this.ax = ax;
    }

    public void setAy(double ay) {
        this.ay = ay;
    }

    public void setAz(double az) {
        this.az = az;
    }

    public void setTeamId(String team_id) {
        this.team_id = team_id;
    }
}
