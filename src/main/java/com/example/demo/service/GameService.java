package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.model.Dto.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
// Note: UUID import removed — code generation uses Random instead

@Service
public class GameService {

    // ── State ────────────────────────────────────────────────────────────────

    // All active games keyed by room code
    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private static final int READY_SECONDS = 3;

    // We inject the messaging template so the service can push broadcasts
    private final SimpMessagingTemplate messaging;

    public GameService(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    // ── Game lifecycle ────────────────────────────────────────────────────────

    /**
     * Creates a new game with a random 4-letter code and pre-loads
     * some sample questions. Returns the game code.
     */
    public String createGame() {
        String code = generateCode();
        Game game = new Game(code);
        game.setQuestions(loadQuestionsFromFile());
        games.put(code, game);
        return code;
    }

    /**
     * Adds a player to the lobby and broadcasts the updated player list.
     * Returns false if the game doesn't exist or has already started.
     */
    public Player joinGame(String code, String playerName, String avatarId, String playerId) {
        Game game = games.get(code);
        if (game == null || game.getStatus() != GameStatus.WAITING) {
            return null;
        }
        Player player = game.joinOrReconnect(playerName.trim(), avatarId != null ? avatarId : "white", playerId);
        broadcastLobby(game);
        return player;
    }

    public boolean leaveGame(String code, String playerId, String playerName) {
        Game game = games.get(code);
        if (game == null || game.getStatus() != GameStatus.WAITING) {
            return false;
        }
        boolean removed;
        synchronized (game) {
            removed = game.removePlayer(playerId, playerName);
        }
        if (removed) {
            broadcastLobby(game);
        }
        return removed;
    }

    /**
     * Host starts the game — moves to the first question.
     */
    public boolean startGame(String code) {
        Game game = games.get(code);
        if (game == null || game.getStatus() != GameStatus.WAITING) {
            return false;
        }
        if (game.getPlayers().isEmpty()) {
            return false;
        }
        prepareNextQuestion(game);
        return true;
    }

    /**
     * Host moves to the next question (called after results screen).
     */
    public boolean nextQuestion(String code) {
        Game game = games.get(code);
        if (game == null || game.getStatus() != GameStatus.RESULTS) {
            return false;
        }
        if (game.hasMoreQuestions()) {
            prepareNextQuestion(game);
        } else {
            finishGame(game);
        }
        return true;
    }

    // ── Answer handling ───────────────────────────────────────────────────────

    /**
     * Called when a player submits an answer via WebSocket.
     * Calculates score (faster = more points), marks the player as answered,
     * and triggers results broadcast if everyone has answered.
     */
    public void submitAnswer(AnswerPayload payload) {
        Game game = games.get(payload.getGameCode());
        if (game == null) {
            return;
        }

        synchronized (game) {
            int questionIndex = game.getCurrentQuestionIndex();
            if (payload.getQuestionNumber() <= 0) {
                return;
            }
            int payloadQuestionIndex = payload.getQuestionNumber() - 1;
            if (game.getStatus() != GameStatus.QUESTION || game.isRoundFinalized() || payloadQuestionIndex != questionIndex) {
                return;
            }
            if (System.currentTimeMillis() >= game.getQuestionEndsAt()) {
                finalizeRound(game, questionIndex);
                return;
            }

            Player player = game.findPlayer(payload.getPlayerId(), payload.getPlayerName());
            if (player == null || player.isAnsweredCurrentQuestion()) {
                return; // unknown player or already answered
            }

            recordAnswer(game, player, payload.getAnswerIndex(), false);

            broadcastAnswerProgress(game);

            if (game.allPlayersAnswered()) {
                finalizeRound(game, questionIndex);
            }
        }
    }

    // ── Getters for REST ──────────────────────────────────────────────────────

    public boolean gameExists(String code) {
        return games.containsKey(code);
    }

    public GameStatus getStatus(String code) {
        Game game = games.get(code);
        return game == null ? null : game.getStatus();
    }

    public GameStateResponse getGameState(String code, String playerId, String playerName) {
        Game game = games.get(code);
        if (game == null) {
            return null;
        }
        synchronized (game) {
            Player player = game.findPlayer(playerId, playerName);
            if (game.getStatus() == GameStatus.QUESTION && !game.isRoundFinalized()
                    && System.currentTimeMillis() >= game.getQuestionEndsAt()) {
                finalizeRound(game, game.getCurrentQuestionIndex());
            }
            return buildGameState(game, player);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void prepareNextQuestion(Game game) {
        synchronized (game) {
            game.setPreviousRanks(buildRankMap(game.getLeaderboard()));
            game.setCurrentQuestionIndex(game.getCurrentQuestionIndex() + 1);
            game.setRoundFinalized(false);
            game.setQuestionStartTime(0);
            game.setQuestionEndsAt(0);
            game.setReadyEndsAt(System.currentTimeMillis() + READY_SECONDS * 1000L);
            game.setStatus(GameStatus.GET_READY);
            game.resetAnswerFlags();

            messaging.convertAndSend("/topic/game/" + game.getCode(), new GetReadyBroadcast(
                    GameStatus.GET_READY,
                    game.getCurrentQuestionIndex() + 1,
                    game.getQuestions().size(),
                    READY_SECONDS
            ));

            int questionIndex = game.getCurrentQuestionIndex();
            CompletableFuture.delayedExecutor(READY_SECONDS, TimeUnit.SECONDS).execute(() -> advanceToQuestion(game, questionIndex));
        }
    }

    private void advanceToQuestion(Game game, int questionIndex) {
        synchronized (game) {
            if (game.getStatus() != GameStatus.GET_READY || game.getCurrentQuestionIndex() != questionIndex) {
                return;
            }
            long now = System.currentTimeMillis();
            game.setStatus(GameStatus.QUESTION);
            game.setRoundFinalized(false);
            game.resetAnswerFlags();
            game.setQuestionStartTime(now);

            Question q = game.currentQuestion();
            game.setQuestionEndsAt(now + q.getTimeLimitSeconds() * 1000L);

            QuestionBroadcast broadcast = new QuestionBroadcast(
                    GameStatus.QUESTION,
                    game.getCurrentQuestionIndex() + 1,   // 1-based
                    game.getQuestions().size(),
                    q.getText(),
                    q.getOptions(),
                    q.getTimeLimitSeconds(),
                    game.getAnsweredCount(),
                    game.getPlayers().size(),
                    game.getQuestionStartTime(),
                    game.getQuestionEndsAt()
            );

            messaging.convertAndSend("/topic/game/" + game.getCode(), broadcast);

            CompletableFuture.delayedExecutor(q.getTimeLimitSeconds(), TimeUnit.SECONDS)
                    .execute(() -> finalizeRound(game, questionIndex));
        }
    }

    private void finalizeRound(Game game, int questionIndex) {
        synchronized (game) {
            if (game.isRoundFinalized() || game.getStatus() != GameStatus.QUESTION
                    || game.getCurrentQuestionIndex() != questionIndex) {
                return;
            }
            game.setRoundFinalized(true);
            for (Player player : game.getPlayers().values()) {
                if (!player.isAnsweredCurrentQuestion()) {
                    recordAnswer(game, player, -1, true);
                }
            }
            broadcastResults(game);
        }
    }

    private void recordAnswer(Game game, Player player, int answerIndex, boolean timedOut) {
        if (player.isAnsweredCurrentQuestion()) {
            return;
        }
        player.setAnsweredCurrentQuestion(true);
        player.setLastAnswerIndex(answerIndex);

        Question q = game.currentQuestion();
        boolean correct = !timedOut && answerIndex == q.getCorrectIndex();
        int points = 0;
        if (correct) {
            long elapsed = System.currentTimeMillis() - game.getQuestionStartTime();
            points = calculatePoints(elapsed, q.getTimeLimitSeconds());
            player.addScore(points);
        }
        player.addAnswerRecord(new AnswerRecord(
                game.getCurrentQuestionIndex(),
                q.getText(),
                answerIndex,
                timedOut ? "No answer" : answerText(q, answerIndex),
                q.getCorrectIndex(),
                answerText(q, q.getCorrectIndex()),
                correct,
                points,
                timedOut
        ));
    }

    private void broadcastResults(Game game) {
        game.setStatus(GameStatus.RESULTS);

        // Count how many players chose each option (0-3)
        int[] answerCounts = new int[4];
        for (Player p : game.getPlayers().values()) {
            int idx = p.getLastAnswerIndex();
            if (idx >= 0 && idx < 4) answerCounts[idx]++;
        }

        List<LeaderboardEntry> board = buildLeaderboard(game);
        Question question = game.currentQuestion();
        ResultsBroadcast broadcast = new ResultsBroadcast(
                GameStatus.RESULTS,
                game.getCurrentQuestionIndex() + 1,
                game.getQuestions().size(),
                question.getText(),
                question.getOptions(),
                question.getCorrectIndex(),
                board,
                answerCounts,
                buildQuestionStats(game, answerCounts),
                buildPlayerQuestionResults(game, board, answerCounts)
        );

        messaging.convertAndSend("/topic/game/" + game.getCode(), broadcast);
    }

    private void finishGame(Game game) {
        game.setStatus(GameStatus.FINISHED);

        FinishedBroadcast broadcast = new FinishedBroadcast(
                GameStatus.FINISHED,
                buildLeaderboard(game),
                buildPlayerReviews(game)
        );

        messaging.convertAndSend("/topic/game/" + game.getCode(), broadcast);

        // Clean up after a short delay (keep entry for a minute so latecomers see score)
        // For simplicity we just leave it; a scheduler could remove it later.
    }

    private void broadcastLobby(Game game) {
        List<PlayerInfo> players = game.getPlayers().values().stream()
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .map(p -> new PlayerInfo(p.getId(), p.getName(), p.getAvatarId()))
                .toList();
        messaging.convertAndSend("/topic/lobby/" + game.getCode(), new LobbyBroadcast(players));
    }

    private List<LeaderboardEntry> buildLeaderboard(Game game) {
        List<Player> sorted = game.getLeaderboard();
        return IntStream.range(0, sorted.size())
                .mapToObj(i -> {
                    int rank = i + 1;
                    int previousRank = game.getPreviousRanks().getOrDefault(sorted.get(i).getName(), rank);
                    int rankDelta = previousRank - rank;
                    return new LeaderboardEntry(
                            sorted.get(i).getName(),
                            sorted.get(i).getScore(),
                            rank,
                            previousRank,
                            rankDelta);
                })
                .toList();
    }

    private Map<String, Integer> buildRankMap(List<Player> leaderboard) {
        Map<String, Integer> ranks = new HashMap<>();
        for (int i = 0; i < leaderboard.size(); i++) {
            ranks.put(leaderboard.get(i).getName(), i + 1);
        }
        return ranks;
    }

    private void broadcastAnswerProgress(Game game) {
        messaging.convertAndSend("/topic/game/" + game.getCode(), new AnswerProgressBroadcast(
                GameStatus.QUESTION,
                game.getAnsweredCount(),
                game.getPlayers().size()
        ));
    }

    private QuestionStats buildQuestionStats(Game game, int[] answerCounts) {
        Question q = game.currentQuestion();
        int answered = game.getAnsweredCount();
        int correct = q == null ? 0 : answerCounts[q.getCorrectIndex()];
        int incorrect = answered - correct;
        int pct = answered == 0 ? 0 : (int) Math.round((correct * 100.0) / answered);
        int questionIndex = game.getCurrentQuestionIndex();
        int pointsAwarded = game.getPlayers().values().stream()
                .flatMap(p -> p.getAnswerHistory().stream())
                .filter(a -> a.getQuestionIndex() == questionIndex)
                .mapToInt(AnswerRecord::getPointsEarned)
                .sum();
        return new QuestionStats(questionIndex + 1, answered, game.getPlayers().size(), correct,
                incorrect, pct, pointsAwarded, game.getTotalScore());
    }

    private List<PlayerReview> buildPlayerReviews(Game game) {
        return game.getPlayers().values().stream()
                .map(player -> {
                    List<AnswerReview> answers = player.getAnswerHistory().stream()
                            .map(a -> new AnswerReview(
                                    a.getQuestionIndex() + 1,
                                    a.getQuestionText(),
                                    a.isCorrect(),
                                    a.getSelectedAnswerIndex(),
                                    a.getSelectedAnswerText(),
                                    a.getCorrectAnswerIndex(),
                                    a.getCorrectAnswerText(),
                                    a.getPointsEarned(),
                                    a.isTimedOut()
                            ))
                            .toList();
                    int correct = (int) answers.stream().filter(AnswerReview::isCorrect).count();
                    int incorrect = answers.size() - correct;
                    int pct = answers.isEmpty() ? 0 : (int) Math.round((correct * 100.0) / answers.size());
                    return new PlayerReview(player.getName(), player.getScore(), correct, incorrect, pct, answers);
                })
                .toList();
    }

    private List<PlayerQuestionResult> buildPlayerQuestionResults(Game game, List<LeaderboardEntry> board, int[] answerCounts) {
        Question q = game.currentQuestion();
        int questionIndex = game.getCurrentQuestionIndex();
        int correctCount = q == null ? 0 : answerCounts[q.getCorrectIndex()];
        int answeredCount = game.getPlayers().size();
        Map<String, LeaderboardEntry> ranks = board.stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getPlayerName(), entry), HashMap::putAll);

        return game.getPlayers().values().stream()
                .map(player -> {
                    AnswerRecord record = player.getAnswerHistory().stream()
                            .filter(a -> a.getQuestionIndex() == questionIndex)
                            .findFirst()
                            .orElse(null);
                    LeaderboardEntry entry = ranks.get(player.getName());
                    int rank = entry == null ? 0 : entry.getRank();
                    int previousRank = entry == null ? rank : entry.getPreviousRank();
                    int rankDelta = entry == null ? 0 : entry.getRankDelta();
                    return new PlayerQuestionResult(
                            player.getName(),
                            record != null && record.isCorrect(),
                            record == null ? 0 : record.getPointsEarned(),
                            player.getScore(),
                            rank,
                            previousRank,
                            rankDelta,
                            currentStreak(player),
                            record == null ? "No answer" : record.getSelectedAnswerText(),
                            record == null ? answerText(q, q == null ? -1 : q.getCorrectIndex()) : record.getCorrectAnswerText(),
                            correctCount,
                            answeredCount
                    );
                })
                .toList();
    }

    private GameStateResponse buildGameState(Game game, Player player) {
        Question q = game.currentQuestion();
        int[] answerCounts = buildAnswerCounts(game);
        List<LeaderboardEntry> board = buildLeaderboard(game);
        QuestionStats stats = game.getStatus() == GameStatus.RESULTS || game.getStatus() == GameStatus.FINISHED
                ? buildQuestionStats(game, answerCounts)
                : null;
        List<PlayerQuestionResult> playerResults = game.getStatus() == GameStatus.RESULTS
                ? buildPlayerQuestionResults(game, board, answerCounts)
                : List.of();
        PlayerQuestionResult playerResult = player == null ? null : playerResults.stream()
                .filter(r -> r.getPlayerName().equals(player.getName()))
                .findFirst()
                .orElse(null);
        PlayerReview playerReview = player == null || game.getStatus() != GameStatus.FINISHED ? null
                : buildPlayerReviews(game).stream()
                .filter(r -> r.getPlayerName().equals(player.getName()))
                .findFirst()
                .orElse(null);

        int readySecondsRemaining = game.getStatus() == GameStatus.GET_READY
                ? Math.max(1, (int) Math.ceil((game.getReadyEndsAt() - System.currentTimeMillis()) / 1000.0))
                : 0;
        return new GameStateResponse(
                game.getStatus(),
                game.getCurrentQuestionIndex() + 1,
                game.getQuestions().size(),
                q == null ? null : q.getText(),
                q == null ? List.of() : q.getOptions(),
                q == null ? 0 : q.getTimeLimitSeconds(),
                game.getQuestionStartTime(),
                game.getQuestionEndsAt(),
                readySecondsRemaining,
                game.getAnsweredCount(),
                game.getPlayers().size(),
                player != null && player.isAnsweredCurrentQuestion(),
                player == null ? -1 : player.getLastAnswerIndex(),
                q == null ? -1 : q.getCorrectIndex(),
                answerCounts,
                board,
                stats,
                playerResult,
                playerReview
        );
    }

    private int[] buildAnswerCounts(Game game) {
        int[] answerCounts = new int[4];
        for (Player p : game.getPlayers().values()) {
            int idx = p.getLastAnswerIndex();
            if (idx >= 0 && idx < 4) answerCounts[idx]++;
        }
        return answerCounts;
    }

    private int currentStreak(Player player) {
        List<AnswerRecord> history = player.getAnswerHistory();
        int streak = 0;
        for (int i = history.size() - 1; i >= 0; i--) {
            if (!history.get(i).isCorrect()) {
                break;
            }
            streak++;
        }
        return streak;
    }

    private String answerText(Question question, int index) {
        if (question == null || index < 0 || index >= question.getOptions().size()) {
            return "No answer";
        }
        return question.getOptions().get(index);
    }

    /**
     * Time-based scoring: answer instantly → 1000 pts, answer at the last second → 500 pts.
     */
    private int calculatePoints(long elapsedMs, int timeLimitSeconds) {
        double fraction = (double) elapsedMs / (timeLimitSeconds * 1000L);
        fraction = Math.min(fraction, 1.0); // cap at 1
        int points = (int) (1000 - (500 * fraction));
        return Math.max(points, 500);
    }

    /** Generates a random 4-letter uppercase code like "ABCD" */
    private String generateCode() {
        Random rng = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder(4);
            for (int i = 0; i < 4; i++) {
                sb.append((char) ('A' + rng.nextInt(26)));
            }
            code = sb.toString();
        } while (games.containsKey(code));
        return code;
    }

    /** Some hard-coded sample questions so you can test right away */
    private List<Question> loadQuestionsFromFile() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            var input = getClass().getClassLoader().getResourceAsStream("questions.json");
            if (input == null) {
                throw new RuntimeException("questions.json not found");
            }

            List<QuestionFileDto> dtos =
                    mapper.readValue(input, new TypeReference<List<QuestionFileDto>>() {});

            return dtos.stream()
                    .map(d -> new Question(
                            d.text,
                            d.options,
                            d.correctIndex,
                            d.timeLimitSeconds > 0 ? d.timeLimitSeconds : 15
                    ))
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load questions.json", e);
        }
    }
}
