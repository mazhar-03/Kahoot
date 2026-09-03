package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private String avatarId = "white";
    private int score = 0;
    private boolean answeredCurrentQuestion = false;
    private int lastAnswerIndex = -1; // track which option they chose
    private final List<AnswerRecord> answerHistory = new ArrayList<>();

    public Player(String name) { this.name = name; }
    public Player(String name, String avatarId) { this.name = name; this.avatarId = avatarId; }

    public String  getName()                           { return name; }
    public String  getAvatarId()                       { return avatarId; }
    public void    setAvatarId(String a)               { this.avatarId = a; }
    public int     getScore()                          { return score; }
    public void    setScore(int s)                     { this.score = s; }
    public boolean isAnsweredCurrentQuestion()         { return answeredCurrentQuestion; }
    public void    setAnsweredCurrentQuestion(boolean b){ this.answeredCurrentQuestion = b; }
    public int     getLastAnswerIndex()                { return lastAnswerIndex; }
    public void    setLastAnswerIndex(int i)           { this.lastAnswerIndex = i; }
    public List<AnswerRecord> getAnswerHistory()        { return answerHistory; }
    public void addScore(int pts)  { this.score += pts; }
    public void addAnswerRecord(AnswerRecord record) { this.answerHistory.add(record); }
    public void resetAnswerFlag()  { this.answeredCurrentQuestion = false; this.lastAnswerIndex = -1; }
}
