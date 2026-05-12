package com.example.demo.service;

import com.example.demo.model.Game;
import com.example.demo.model.GameStatus;
import com.example.demo.model.Player;
import com.example.demo.model.Question;
import com.example.demo.model.Dto.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
// Note: UUID import removed — code generation uses Random instead

@Service
public class GameService {

    // ── State ────────────────────────────────────────────────────────────────

    // All active games keyed by room code
    private final Map<String, Game> games = new ConcurrentHashMap<>();

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
        game.setQuestions(sampleQuestions());
        games.put(code, game);
        return code;
    }

    /**
     * Adds a player to the lobby and broadcasts the updated player list.
     * Returns false if the game doesn't exist or has already started.
     */
    public boolean joinGame(String code, String playerName) {
        Game game = games.get(code);
        if (game == null || game.getStatus() != GameStatus.WAITING) {
            return false;
        }
        game.addPlayer(playerName.trim());
        broadcastLobby(game);
        return true;
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
        advanceToNextQuestion(game);
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
            advanceToNextQuestion(game);
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
        if (game == null || game.getStatus() != GameStatus.QUESTION) {
            return;
        }

        Player player = game.getPlayers().get(payload.getPlayerName());
        if (player == null || player.isAnsweredCurrentQuestion()) {
            return; // unknown player or already answered
        }

        // Mark answered immediately so they can't answer again
        player.setAnsweredCurrentQuestion(true);

        // Score = correct answer? → time-based bonus (max 1000, min 500)
        Question q = game.currentQuestion();
        if (payload.getAnswerIndex() == q.getCorrectIndex()) {
            long elapsed = System.currentTimeMillis() - game.getQuestionStartTime();
            int points = calculatePoints(elapsed, q.getTimeLimitSeconds());
            player.addScore(points);
        }

        // If everyone answered, show results right away
        if (game.allPlayersAnswered()) {
            broadcastResults(game);
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

    // ── Private helpers ───────────────────────────────────────────────────────

    private void advanceToNextQuestion(Game game) {
        game.setCurrentQuestionIndex(game.getCurrentQuestionIndex() + 1);
        game.setStatus(GameStatus.QUESTION);
        game.resetAnswerFlags();
        game.setQuestionStartTime(System.currentTimeMillis());

        Question q = game.currentQuestion();

        QuestionBroadcast broadcast = new QuestionBroadcast(
                GameStatus.QUESTION,
                game.getCurrentQuestionIndex() + 1,   // 1-based
                game.getQuestions().size(),
                q.getText(),
                q.getOptions(),
                q.getTimeLimitSeconds()
        );

        messaging.convertAndSend("/topic/game/" + game.getCode(), broadcast);
    }

    private void broadcastResults(Game game) {
        game.setStatus(GameStatus.RESULTS);

        List<LeaderboardEntry> board = buildLeaderboard(game);
        ResultsBroadcast broadcast = new ResultsBroadcast(
                GameStatus.RESULTS,
                game.currentQuestion().getCorrectIndex(),
                board
        );

        messaging.convertAndSend("/topic/game/" + game.getCode(), broadcast);
    }

    private void finishGame(Game game) {
        game.setStatus(GameStatus.FINISHED);

        FinishedBroadcast broadcast = new FinishedBroadcast(
                GameStatus.FINISHED,
                buildLeaderboard(game)
        );

        messaging.convertAndSend("/topic/game/" + game.getCode(), broadcast);

        // Clean up after a short delay (keep entry for a minute so latecomers see score)
        // For simplicity we just leave it; a scheduler could remove it later.
    }

    private void broadcastLobby(Game game) {
        List<String> names = game.getPlayers().keySet().stream().sorted().toList();
        messaging.convertAndSend("/topic/lobby/" + game.getCode(), new LobbyBroadcast(names));
    }

    private List<LeaderboardEntry> buildLeaderboard(Game game) {
        List<Player> sorted = game.getLeaderboard();
        return IntStream.range(0, sorted.size())
                .mapToObj(i -> new LeaderboardEntry(
                        sorted.get(i).getName(),
                        sorted.get(i).getScore(),
                        i + 1))
                .toList();
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
    private List<Question> sampleQuestions() {
        return List.of(
                new Question(
                        "What is the capital of France?",
                        List.of("Berlin", "Madrid", "Paris", "Rome"),
                        2, 20),
                new Question(
                        "Which planet is closest to the Sun?",
                        List.of("Venus", "Mercury", "Earth", "Mars"),
                        1, 20),
                new Question(
                        "What is 7 × 8?",
                        List.of("54", "56", "64", "48"),
                        1, 15),
                new Question(
                        "Who wrote 'Hamlet'?",
                        List.of("Charles Dickens", "Leo Tolstoy", "William Shakespeare", "Mark Twain"),
                        2, 20),
                new Question(
                        "What does HTTP stand for?",
                        List.of(
                                "HyperText Transfer Protocol",
                                "High Transfer Text Protocol",
                                "HyperText Transmission Process",
                                "Hyper Transfer Technology Protocol"),
                        0, 25)
        );
    }
}