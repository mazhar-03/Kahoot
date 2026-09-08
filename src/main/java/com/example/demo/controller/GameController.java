package com.example.demo.controller;

import com.example.demo.model.Dto.*;
import com.example.demo.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    // railway test checkpoint
    // ── POST /api/game/create ────────────────────────────────────────────────
    // Host calls this to create a new game room.
    // Returns the 4-letter code that players use to join.
    @PostMapping("/create")
    public ResponseEntity<CreateGameResponse> createGame() {
        String code = gameService.createGame();
        return ResponseEntity.ok(new CreateGameResponse(code));
    }

    // ── POST /api/game/{code}/join ───────────────────────────────────────────
    // Player calls this with their chosen name.
    // Server broadcasts updated player list to /topic/lobby/{code}.
    @PostMapping("/{code}/join")
    public ResponseEntity<ApiResponse> joinGame(
            @PathVariable String code,
            @RequestBody JoinGameRequest request) {

        if (request.getPlayerName() == null || request.getPlayerName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Player name cannot be empty"));
        }

        boolean joined = gameService.joinGame(code.toUpperCase(), request.getPlayerName(), request.getAvatarId());

        if (!joined) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Game not found or already started"));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Joined game " + code));
    }

    // ── POST /api/game/{code}/start ──────────────────────────────────────────
    // Host calls this to move from lobby → first question.
    // Broadcasts first QuestionBroadcast to /topic/game/{code}.
    @PostMapping("/{code}/start")
    public ResponseEntity<ApiResponse> startGame(@PathVariable String code) {
        boolean started = gameService.startGame(code.toUpperCase());

        if (!started) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Cannot start game (not found, already started, or no players)"));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Game started"));
    }

    // ── POST /api/game/{code}/next ───────────────────────────────────────────
    // Host calls this after the results screen to advance to the next question,
    // or to finish the game if there are no more questions.
    @PostMapping("/{code}/next")
    public ResponseEntity<ApiResponse> nextQuestion(@PathVariable String code) {
        boolean advanced = gameService.nextQuestion(code.toUpperCase());

        if (!advanced) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Cannot advance (game not in RESULTS state)"));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Advanced to next question (or finished)"));
    }

    // ── GET /api/game/{code}/status ──────────────────────────────────────────
    // Optional: useful for debugging — returns current game status.
    @GetMapping("/{code}/status")
    public ResponseEntity<?> getStatus(@PathVariable String code) {
        var status = gameService.getStatus(code.toUpperCase());
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse(true, status.name()));
    }
}
