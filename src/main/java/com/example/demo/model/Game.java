package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Game {

    private final String code;
    private List<Question> questions = new ArrayList<>();
    private Map<String, Player> players = new ConcurrentHashMap<>();
    private int currentQuestionIndex = -1;
    private GameStatus status = GameStatus.WAITING;
    private long questionStartTime;

    public Game(String code) { this.code = code; }

    public String      getCode()                 { return code; }
    public List<Question> getQuestions()         { return questions; }
    public void        setQuestions(List<Question> q) { this.questions = q; }
    public Map<String, Player> getPlayers()      { return players; }
    public int         getCurrentQuestionIndex() { return currentQuestionIndex; }
    public void        setCurrentQuestionIndex(int i) { this.currentQuestionIndex = i; }
    public GameStatus  getStatus()               { return status; }
    public void        setStatus(GameStatus s)   { this.status = s; }
    public long        getQuestionStartTime()    { return questionStartTime; }
    public void        setQuestionStartTime(long t) { this.questionStartTime = t; }

    public Question currentQuestion() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) return null;
        return questions.get(currentQuestionIndex);
    }

    public boolean hasMoreQuestions() {
        return currentQuestionIndex < questions.size() - 1;
    }

    public void addPlayer(String name) {
        players.putIfAbsent(name, new Player(name));
    }

    public void resetAnswerFlags() {
        players.values().forEach(Player::resetAnswerFlag);
    }

    public boolean allPlayersAnswered() {
        return players.values().stream().allMatch(Player::isAnsweredCurrentQuestion);
    }

    public List<Player> getLeaderboard() {
        return players.values().stream()
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .toList();
    }
}