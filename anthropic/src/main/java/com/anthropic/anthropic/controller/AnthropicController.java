package com.anthropic.anthropic.controller;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.MessageCreateParams;
import com.anthropic.models.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AnthropicController {

    private final AnthropicClient client;
    private final ResourceLoader resourceLoader;

    public AnthropicController(@Value("${anthropic.api.key}") String apiKey, ResourceLoader resourceLoader) {
        this.client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey).build();
        this.resourceLoader = resourceLoader;
    }

    private String loadSystemPrompt() throws IOException {
        try{
            Resource resource = resourceLoader.getResource("file:/app/prompts/system_prompt.txt");
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load system prompt: " + e.getMessage(), e);
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chatWithClaude(@RequestBody(required = false) String jsonNewsArticles) {
        if(jsonNewsArticles == null || jsonNewsArticles.isEmpty()){
            return ResponseEntity.badRequest().body("Request body cannot be empty");
        }

        try {
        String systemPrompt = loadSystemPrompt();

        String fullPrompt = String.format("<json_news_articles>\n%s\n</json_news_articles>\n\n%s", jsonNewsArticles, systemPrompt);


        MessageCreateParams createParams = MessageCreateParams.builder()
                .model(Model.CLAUDE_3_5_SONNET_LATEST)
                .maxTokens(4096)
                .addUserMessage(fullPrompt)
                .build();

        List<String> response = client.messages().create(createParams).content().stream()
                .flatMap(contentBlock -> contentBlock.text().stream())
                .map(textBlock -> textBlock.text())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }catch (RuntimeException | IOException e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


}
