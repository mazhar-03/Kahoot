package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Game {

    private final String code;
    private List<Question> questions = new ArrayList<>();
    private Map<String, Player> players = new ConcurrentHashMap<>();
    private int currentQuestionIndex = -1;
    private GameStatus status = GameStatus.WAITING;
    private long questionStartTime;
    private long questionEndsAt;
    private long readyEndsAt;
    private boolean roundFinalized = false;
    private Map<String, Integer> previousRanks = new ConcurrentHashMap<>();

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
    public long        getQuestionEndsAt()       { return questionEndsAt; }
    public void        setQuestionEndsAt(long t) { this.questionEndsAt = t; }
    public long        getReadyEndsAt()          { return readyEndsAt; }
    public void        setReadyEndsAt(long t)    { this.readyEndsAt = t; }
    public boolean     isRoundFinalized()        { return roundFinalized; }
    public void        setRoundFinalized(boolean v) { this.roundFinalized = v; }
    public Map<String, Integer> getPreviousRanks() { return previousRanks; }
    public void setPreviousRanks(Map<String, Integer> ranks) { this.previousRanks = new ConcurrentHashMap<>(ranks); }

    public Question currentQuestion() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) return null;
        return questions.get(currentQuestionIndex);
    }

    public boolean hasMoreQuestions() {
        return currentQuestionIndex < questions.size() - 1;
    }

    public void addPlayer(String name) { players.putIfAbsent(name, new Player(name)); }
    public void addPlayer(String name, String avatarId) { players.putIfAbsent(name, new Player(name, avatarId)); }

    public Player joinOrReconnect(String name, String avatarId, String playerId) {
        Player existing = findPlayer(playerId, name);
        if (existing != null) {
            if (avatarId != null && !avatarId.isBlank()) {
                existing.setAvatarId(avatarId);
            }
            return existing;
        }
        Player created = new Player(playerId == null || playerId.isBlank() ? UUID.randomUUID().toString() : playerId,
                name, avatarId != null ? avatarId : "white");
        Player previous = players.putIfAbsent(name, created);
        return previous != null ? previous : created;
    }

    public Player findPlayer(String playerId, String playerName) {
        if (playerId != null && !playerId.isBlank()) {
            for (Player player : players.values()) {
                if (playerId.equals(player.getId())) {
                    return player;
                }
            }
        }
        if (playerName == null) {
            return null;
        }
        return players.get(playerName);
    }

    public boolean removePlayer(String playerId, String playerName) {
        Player player = findPlayer(playerId, playerName);
        if (player == null) {
            return false;
        }
        return players.remove(player.getName(), player);
    }

    public void resetAnswerFlags() {
        players.values().forEach(Player::resetAnswerFlag);
    }

    public int getAnsweredCount() {
        return (int) players.values().stream().filter(Player::isAnsweredCurrentQuestion).count();
    }

    public int getTotalScore() {
        return players.values().stream().mapToInt(Player::getScore).sum();
    }

    public boolean allPlayersAnswered() {
        return players.values().stream().allMatch(Player::isAnsweredCurrentQuestion);
    }

    public List<Player> getLeaderboard() {
        return players.values().stream()
                .sorted(java.util.Comparator.comparingInt(Player::getScore).reversed()
                        .thenComparing(Player::getName))
                .toList();
    }
}
