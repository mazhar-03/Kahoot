package com.example.demo.model;

import java.util.List;

public class Dto {

    public static class AnswerPayload {
        private String gameCode;
        private String playerName;
        private int answerIndex;
        public AnswerPayload() {}
        public String getGameCode()    { return gameCode; }
        public String getPlayerName()  { return playerName; }
        public int    getAnswerIndex() { return answerIndex; }
        public void setGameCode(String v)   { this.gameCode = v; }
        public void setPlayerName(String v) { this.playerName = v; }
        public void setAnswerIndex(int v)   { this.answerIndex = v; }
    }

    public static class QuestionBroadcast {
        private GameStatus status;
        private int questionNumber;
        private int totalQuestions;
        private String questionText;
        private List<String> options;
        private int timeLimitSeconds;
        public QuestionBroadcast() {}
        public QuestionBroadcast(GameStatus status, int questionNumber, int totalQuestions,
                                 String questionText, List<String> options, int timeLimitSeconds) {
            this.status = status; this.questionNumber = questionNumber;
            this.totalQuestions = totalQuestions; this.questionText = questionText;
            this.options = options; this.timeLimitSeconds = timeLimitSeconds;
        }
        public GameStatus   getStatus()           { return status; }
        public int          getQuestionNumber()   { return questionNumber; }
        public int          getTotalQuestions()   { return totalQuestions; }
        public String       getQuestionText()     { return questionText; }
        public List<String> getOptions()          { return options; }
        public int          getTimeLimitSeconds() { return timeLimitSeconds; }
    }

    public static class ResultsBroadcast {
        private GameStatus status;
        private int correctIndex;
        private List<LeaderboardEntry> leaderboard;
        private int[] answerCounts; // how many players picked each option
        public ResultsBroadcast() {}
        public ResultsBroadcast(GameStatus status, int correctIndex, List<LeaderboardEntry> leaderboard, int[] answerCounts) {
            this.status = status; this.correctIndex = correctIndex;
            this.leaderboard = leaderboard; this.answerCounts = answerCounts;
        }
        public GameStatus             getStatus()       { return status; }
        public int                    getCorrectIndex() { return correctIndex; }
        public List<LeaderboardEntry> getLeaderboard()  { return leaderboard; }
        public int[]                  getAnswerCounts() { return answerCounts; }
    }

    public static class FinishedBroadcast {
        private GameStatus status;
        private List<LeaderboardEntry> leaderboard;
        public FinishedBroadcast() {}
        public FinishedBroadcast(GameStatus status, List<LeaderboardEntry> leaderboard) {
            this.status = status; this.leaderboard = leaderboard;
        }
        public GameStatus             getStatus()      { return status; }
        public List<LeaderboardEntry> getLeaderboard() { return leaderboard; }
    }

    public static class LeaderboardEntry {
        private String playerName;
        private int score;
        private int rank;
        public LeaderboardEntry() {}
        public LeaderboardEntry(String playerName, int score, int rank) {
            this.playerName = playerName; this.score = score; this.rank = rank;
        }
        public String getPlayerName() { return playerName; }
        public int    getScore()      { return score; }
        public int    getRank()       { return rank; }
    }

    /** Lobby broadcast now includes avatarId per player */
    public static class PlayerInfo {
        private String name;
        private String avatarId;
        public PlayerInfo() {}
        public PlayerInfo(String name, String avatarId) { this.name = name; this.avatarId = avatarId; }
        public String getName()     { return name; }
        public String getAvatarId() { return avatarId; }
    }

    public static class LobbyBroadcast {
        private List<PlayerInfo> players;
        public LobbyBroadcast() {}
        public LobbyBroadcast(List<PlayerInfo> players) { this.players = players; }
        public List<PlayerInfo> getPlayers() { return players; }
    }

    public static class CreateGameResponse {
        private String gameCode;
        public CreateGameResponse() {}
        public CreateGameResponse(String gameCode) { this.gameCode = gameCode; }
        public String getGameCode() { return gameCode; }
    }

    /** Now includes avatarId */
    public static class JoinGameRequest {
        private String playerName;
        private String avatarId;
        public JoinGameRequest() {}
        public String getPlayerName()         { return playerName; }
        public void   setPlayerName(String v) { this.playerName = v; }
        public String getAvatarId()           { return avatarId; }
        public void   setAvatarId(String v)   { this.avatarId = v; }
    }

    public static class ApiResponse {
        private boolean success;
        private String message;
        public ApiResponse() {}
        public ApiResponse(boolean success, String message) { this.success = success; this.message = message; }
        public boolean isSuccess()  { return success; }
        public String  getMessage() { return message; }
    }
}