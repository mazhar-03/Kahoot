package com.example.demo.controller;

import com.example.demo.model.Dto.AnswerPayload;
import com.example.demo.service.GameService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final GameService gameService;

    public WebSocketController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Players send their answer to /app/answer
     *
     * Payload (JSON):
     * {
     *   "gameCode":    "ABCD",
     *   "playerName":  "Alice",
     *   "answerIndex": 2
     * }
     *
     * The service handles scoring + broadcasting results when everyone has answered.
     */
    @MessageMapping("/answer")
    public void handleAnswer(AnswerPayload payload) {
        gameService.submitAnswer(payload);
    }
}