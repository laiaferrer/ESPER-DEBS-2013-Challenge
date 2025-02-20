package com;

public class RunningStatistics {
        private long ts;                 // Timestamp
        private String player_id;           // Player ID
        private long standing_time;    // Time spent standing
        private double standing_distance; // Distance covered while standing
        private long trot_time;        // Time spent trotting
        private double trot_distance;    // Distance covered while trotting
        private long low_time;         // Time spent at low intensity
        private double low_distance;     // Distance covered at low intensity
        private long medium_time;      // Time spent at medium intensity
        private double medium_distance;  // Distance covered at medium intensity
        private long high_time;        // Time spent at high intensity
        private double high_distance;    // Distance covered at high intensity
        private long sprint_time;      // Time spent sprinting
        private double sprint_distance;  // Distance covered while sprinting
    
        // Constructor
        public RunningStatistics(long ts, String player_id, long standing_time, double standing_distance, 
                                 long trot_time, double trot_distance, long low_time, double low_distance, 
                                 long medium_time, double medium_distance, long high_time, double high_distance, 
                                 long sprint_time, double sprint_distance) {
            this.ts = ts;
            this.player_id = player_id;
            this.standing_time = standing_time;
            this.standing_distance = standing_distance;
            this.trot_time = trot_time;
            this.trot_distance = trot_distance;
            this.low_time = low_time;
            this.low_distance = low_distance;
            this.medium_time = medium_time;
            this.medium_distance = medium_distance;
            this.high_time = high_time;
            this.high_distance = high_distance;
            this.sprint_time = sprint_time;
            this.sprint_distance = sprint_distance;
        }
    
        // Getters and Setters
        public long getTs() {
            return ts;
        }
    
        public void setTs(long ts) {
            this.ts = ts;
        }
    
        public String getPlayer_id() {
            return player_id;
        }
    
        public void setPlayer_id(String player_id) {
            this.player_id = player_id;
        }
    
        public long getStanding_time() {
            return standing_time;
        }
    
        public void setStanding_time(long standing_time) {
            this.standing_time = standing_time;
        }
    
        public double getStanding_distance() {
            return standing_distance;
        }
    
        public void setStanding_distance(double standing_distance) {
            this.standing_distance = standing_distance;
        }
    
        public long getTrot_time() {
            return trot_time;
        }
    
        public void setTrot_time(long trot_time) {
            this.trot_time = trot_time;
        }
    
        public double getTrot_distance() {
            return trot_distance;
        }
    
        public void setTrot_distance(double trot_distance) {
            this.trot_distance = trot_distance;
        }
    
        public long getLow_time() {
            return low_time;
        }
    
        public void setLow_time(long low_time) {
            this.low_time = low_time;
        }
    
        public double getLow_distance() {
            return low_distance;
        }
    
        public void setLow_distance(double low_distance) {
            this.low_distance = low_distance;
        }
    
        public long getMedium_time() {
            return medium_time;
        }
    
        public void setMedium_time(long medium_time) {
            this.medium_time = medium_time;
        }
    
        public double getMedium_distance() {
            return medium_distance;
        }
    
        public void setMedium_distance(double medium_distance) {
            this.medium_distance = medium_distance;
        }
    
        public long getHigh_time() {
            return high_time;
        }
    
        public void setHigh_time(long high_time) {
            this.high_time = high_time;
        }
    
        public double getHigh_distance() {
            return high_distance;
        }
    
        public void setHigh_distance(double high_distance) {
            this.high_distance = high_distance;
        }
    
        public long getSprint_time() {
            return sprint_time;
        }
    
        public void setSprint_time(long sprint_time) {
            this.sprint_time = sprint_time;
        }
    
        public double getSprint_distance() {
            return sprint_distance;
        }
    
        public void setSprint_distance(double sprint_distance) {
            this.sprint_distance = sprint_distance;
        }
}
