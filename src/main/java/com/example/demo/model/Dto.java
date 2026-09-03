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
        private int answeredCount;
        private int totalPlayers;
        public QuestionBroadcast() {}
        public QuestionBroadcast(GameStatus status, int questionNumber, int totalQuestions,
                                 String questionText, List<String> options, int timeLimitSeconds,
                                 int answeredCount, int totalPlayers) {
            this.status = status; this.questionNumber = questionNumber;
            this.totalQuestions = totalQuestions; this.questionText = questionText;
            this.options = options; this.timeLimitSeconds = timeLimitSeconds;
            this.answeredCount = answeredCount; this.totalPlayers = totalPlayers;
        }
        public GameStatus   getStatus()           { return status; }
        public int          getQuestionNumber()   { return questionNumber; }
        public int          getTotalQuestions()   { return totalQuestions; }
        public String       getQuestionText()     { return questionText; }
        public List<String> getOptions()          { return options; }
        public int          getTimeLimitSeconds() { return timeLimitSeconds; }
        public int          getAnsweredCount()    { return answeredCount; }
        public int          getTotalPlayers()     { return totalPlayers; }
    }

    public static class AnswerProgressBroadcast {
        private GameStatus status;
        private int answeredCount;
        private int totalPlayers;
        public AnswerProgressBroadcast() {}
        public AnswerProgressBroadcast(GameStatus status, int answeredCount, int totalPlayers) {
            this.status = status; this.answeredCount = answeredCount; this.totalPlayers = totalPlayers;
        }
        public GameStatus getStatus()    { return status; }
        public int getAnsweredCount()    { return answeredCount; }
        public int getTotalPlayers()     { return totalPlayers; }
    }

    public static class GetReadyBroadcast {
        private GameStatus status;
        private int questionNumber;
        private int totalQuestions;
        private int readySeconds;
        public GetReadyBroadcast() {}
        public GetReadyBroadcast(GameStatus status, int questionNumber, int totalQuestions, int readySeconds) {
            this.status = status; this.questionNumber = questionNumber;
            this.totalQuestions = totalQuestions; this.readySeconds = readySeconds;
        }
        public GameStatus getStatus() { return status; }
        public int getQuestionNumber() { return questionNumber; }
        public int getTotalQuestions() { return totalQuestions; }
        public int getReadySeconds() { return readySeconds; }
    }

    public static class ResultsBroadcast {
        private GameStatus status;
        private int correctIndex;
        private List<LeaderboardEntry> leaderboard;
        private int[] answerCounts; // how many players picked each option
        private QuestionStats questionStats;
        private List<PlayerQuestionResult> playerResults;
        public ResultsBroadcast() {}
        public ResultsBroadcast(GameStatus status, int correctIndex, List<LeaderboardEntry> leaderboard,
                                int[] answerCounts, QuestionStats questionStats, List<PlayerQuestionResult> playerResults) {
            this.status = status; this.correctIndex = correctIndex;
            this.leaderboard = leaderboard; this.answerCounts = answerCounts; this.questionStats = questionStats;
            this.playerResults = playerResults;
        }
        public GameStatus             getStatus()       { return status; }
        public int                    getCorrectIndex() { return correctIndex; }
        public List<LeaderboardEntry> getLeaderboard()  { return leaderboard; }
        public int[]                  getAnswerCounts() { return answerCounts; }
        public QuestionStats          getQuestionStats(){ return questionStats; }
        public List<PlayerQuestionResult> getPlayerResults() { return playerResults; }
    }

    public static class PlayerQuestionResult {
        private String playerName;
        private boolean correct;
        private int pointsEarned;
        private int totalScore;
        private int rank;
        private int previousRank;
        private int rankDelta;
        private int streak;
        private String selectedAnswerText;
        private String correctAnswerText;
        private int correctCount;
        private int answeredCount;
        public PlayerQuestionResult() {}
        public PlayerQuestionResult(String playerName, boolean correct, int pointsEarned, int totalScore,
                                    int rank, int previousRank, int rankDelta, int streak,
                                    String selectedAnswerText, String correctAnswerText,
                                    int correctCount, int answeredCount) {
            this.playerName = playerName; this.correct = correct; this.pointsEarned = pointsEarned;
            this.totalScore = totalScore; this.rank = rank; this.previousRank = previousRank;
            this.rankDelta = rankDelta; this.streak = streak; this.selectedAnswerText = selectedAnswerText;
            this.correctAnswerText = correctAnswerText; this.correctCount = correctCount; this.answeredCount = answeredCount;
        }
        public String getPlayerName() { return playerName; }
        public boolean isCorrect() { return correct; }
        public int getPointsEarned() { return pointsEarned; }
        public int getTotalScore() { return totalScore; }
        public int getRank() { return rank; }
        public int getPreviousRank() { return previousRank; }
        public int getRankDelta() { return rankDelta; }
        public int getStreak() { return streak; }
        public String getSelectedAnswerText() { return selectedAnswerText; }
        public String getCorrectAnswerText() { return correctAnswerText; }
        public int getCorrectCount() { return correctCount; }
        public int getAnsweredCount() { return answeredCount; }
    }

    public static class FinishedBroadcast {
        private GameStatus status;
        private List<LeaderboardEntry> leaderboard;
        private List<PlayerReview> playerReviews;
        public FinishedBroadcast() {}
        public FinishedBroadcast(GameStatus status, List<LeaderboardEntry> leaderboard, List<PlayerReview> playerReviews) {
            this.status = status; this.leaderboard = leaderboard; this.playerReviews = playerReviews;
        }
        public GameStatus             getStatus()      { return status; }
        public List<LeaderboardEntry> getLeaderboard() { return leaderboard; }
        public List<PlayerReview>     getPlayerReviews(){ return playerReviews; }
    }

    public static class PlayerReview {
        private String playerName;
        private int finalScore;
        private int correctCount;
        private int incorrectCount;
        private int accuracyPercentage;
        private List<AnswerReview> answers;
        public PlayerReview() {}
        public PlayerReview(String playerName, int finalScore, int correctCount, int incorrectCount,
                            int accuracyPercentage, List<AnswerReview> answers) {
            this.playerName = playerName; this.finalScore = finalScore; this.correctCount = correctCount;
            this.incorrectCount = incorrectCount; this.accuracyPercentage = accuracyPercentage; this.answers = answers;
        }
        public String getPlayerName() { return playerName; }
        public int getFinalScore() { return finalScore; }
        public int getCorrectCount() { return correctCount; }
        public int getIncorrectCount() { return incorrectCount; }
        public int getAccuracyPercentage() { return accuracyPercentage; }
        public List<AnswerReview> getAnswers() { return answers; }
    }

    public static class AnswerReview {
        private int questionNumber;
        private String questionText;
        private boolean correct;
        private int selectedAnswerIndex;
        private String selectedAnswerText;
        private int correctAnswerIndex;
        private String correctAnswerText;
        private int pointsEarned;
        public AnswerReview() {}
        public AnswerReview(int questionNumber, String questionText, boolean correct, int selectedAnswerIndex,
                            String selectedAnswerText, int correctAnswerIndex, String correctAnswerText, int pointsEarned) {
            this.questionNumber = questionNumber; this.questionText = questionText; this.correct = correct;
            this.selectedAnswerIndex = selectedAnswerIndex; this.selectedAnswerText = selectedAnswerText;
            this.correctAnswerIndex = correctAnswerIndex; this.correctAnswerText = correctAnswerText;
            this.pointsEarned = pointsEarned;
        }
        public int getQuestionNumber() { return questionNumber; }
        public String getQuestionText() { return questionText; }
        public boolean isCorrect() { return correct; }
        public int getSelectedAnswerIndex() { return selectedAnswerIndex; }
        public String getSelectedAnswerText() { return selectedAnswerText; }
        public int getCorrectAnswerIndex() { return correctAnswerIndex; }
        public String getCorrectAnswerText() { return correctAnswerText; }
        public int getPointsEarned() { return pointsEarned; }
    }

    public static class LeaderboardEntry {
        private String playerName;
        private int score;
        private int rank;
        private int previousRank;
        private int rankDelta;
        public LeaderboardEntry() {}
        public LeaderboardEntry(String playerName, int score, int rank, int previousRank, int rankDelta) {
            this.playerName = playerName; this.score = score; this.rank = rank;
            this.previousRank = previousRank; this.rankDelta = rankDelta;
        }
        public String getPlayerName() { return playerName; }
        public int    getScore()      { return score; }
        public int    getRank()       { return rank; }
        public int    getPreviousRank(){ return previousRank; }
        public int    getRankDelta()  { return rankDelta; }
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
