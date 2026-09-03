package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class App {

    private static final String BOT_ID = System.getenv("BOT_ID");
    private static final String GROUPME_POST_URL = "https://api.groupme.com/v3/bots/post";

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @PostMapping("/")
    public Map<String, String> webhook(@RequestBody Map<String, Object> payload) {
        Map<String, String> response = new HashMap<>();

        // Prevent the bot from processing its own messages
        String senderType = (String) payload.get("sender_type");
        if (!"user".equals(senderType)) {
            response.put("status", "ignored");
            return response;
        }

        String text = (String) payload.get("text");
        String senderName = (String) payload.getOrDefault("name", "Player");

        if (text != null && (text.contains("!roll") || text.contains("!play"))) {
            int userNum = (int) (Math.random() * 100) + 1;
            int botNum = (int) (Math.random() * 100) + 1;

            String result;
            if (userNum > botNum) {
                result = senderName + " wins!";
            } else if (botNum > userNum) {
                result = "The Bot wins!";
            } else {
                result = "It's a tie!";
            }

            String reply = String.format(
                "🎲 Game Time!\n%s rolled: %d\nBot rolled: %d\n%s",
                senderName, userNum, botNum, result
            );

            sendGroupMeMessage(reply);
        }

        response.put("status", "ok");
        return response;
    }

    private void sendGroupMeMessage(String messageText) {
        if (BOT_ID == null || BOT_ID.isEmpty()) return;

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> body = new HashMap<>();
        body.put("bot_id", BOT_ID);
        body.put("text", messageText);

        try {
            restTemplate.postForEntity(GROUPME_POST_URL, body, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
