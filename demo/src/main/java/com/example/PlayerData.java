package com.example;

import java.util.List;

public class PlayerData {
    private String playerName;
    private String teamName;
    private List<Integer> sensorIds;

    public PlayerData(String playerName, String teamName, List<Integer> sensorIds) {
        this.playerName = playerName;
        this.teamName = teamName;
        this.sensorIds = sensorIds;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getTeamName() {
        return teamName;
    }

    public List<Integer> getSensorIds() {
        return sensorIds;
    }
}
