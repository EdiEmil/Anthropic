package com.anthropic.anthropic.controller;

import com.anthropic.anthropic.dto.ChatRequest;
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.MessageCreateParams;
import com.anthropic.models.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AnthropicController {

    private final AnthropicClient client;

    public AnthropicController(@Value("${anthropic.api.key}") String apiKey) {
        this.client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey).build();
    }

    @PostMapping("/chat")
    public List<String> chatWithClaude(@RequestBody ChatRequest request) {
        MessageCreateParams createParams = MessageCreateParams.builder()
                .model(Model.CLAUDE_3_5_SONNET_LATEST)
                .maxTokens(2048)
                .addUserMessage(request.getMessage())
                .build();

        return client.messages().create(createParams).content().stream()
                .flatMap(contentBlock -> contentBlock.text().stream())
                .map(textBlock -> textBlock.text())
                .collect(Collectors.toList());
    }


}
