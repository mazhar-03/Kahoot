package com.example.demo.model;

public class Player {

    private final String name;
    private int score = 0;
    private boolean answeredCurrentQuestion = false;

    public Player(String name) { this.name = name; }

    public String  getName()                      { return name; }
    public int     getScore()                     { return score; }
    public void    setScore(int s)                { this.score = s; }
    public boolean isAnsweredCurrentQuestion()    { return answeredCurrentQuestion; }
    public void    setAnsweredCurrentQuestion(boolean b) { this.answeredCurrentQuestion = b; }

    public void addScore(int points)  { this.score += points; }
    public void resetAnswerFlag()     { this.answeredCurrentQuestion = false; }
}