package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GameStatus {
    WAITING,    // lobby - waiting for players to join
    GET_READY,  // short synchronized countdown before a question starts
    QUESTION,   // a question is currently active
    RESULTS,    // showing results/leaderboard after a question
    FINISHED;   // game over, final scores shown

    @JsonValue
    public String toLowerCase() {
        return this.name().toLowerCase();
    }
}
