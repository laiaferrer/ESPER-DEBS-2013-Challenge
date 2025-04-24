package com.example;

public class BallEvent {

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


    // Constructor to initialize all fields
    public BallEvent(String sid, long ts, double x, double y, double z, double v, double a,
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
}

