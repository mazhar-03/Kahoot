package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // Serves templates/index.html  → the landing page (join or host)
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Serves templates/host.html   → the host view
    @GetMapping("/host")
    public String host() {
        return "host";
    }

    // Serves templates/player.html → the player view
    @GetMapping("/player")
    public String player() {
        return "player";
    }
}