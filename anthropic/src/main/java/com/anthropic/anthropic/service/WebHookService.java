package com.anthropic.anthropic.service;


import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Service
public class WebHookService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendWebhook(String webhookUrl, List<String> jsonResponse) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<String>> request = new HttpEntity<>(jsonResponse, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(webhookUrl, HttpMethod.POST, request, String.class);
            System.out.println("Webhook sent successfully: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("Failed to send webhook: " + e.getMessage());
        }
    }
}
