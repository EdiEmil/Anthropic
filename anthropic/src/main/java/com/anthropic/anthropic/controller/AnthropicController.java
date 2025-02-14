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
    public List<String> chatWithClaude(@RequestBody String jsonNewsArticles) {

        String systemPrompt = """
            <json_news_articles>
            %s
            </json_news_articles>

            You are tasked with identifying potential clinical trial opportunities from a file containing news articles you can find from the JSON formatted articles file I provide.

            Your goal is to identify clinical trial opportunities based on the following criteria:
            1. A compound or medicine moving from preclinical phase to phase 1
            2. A progression from phase 1 to phase 2 with positive results from previous trials
            3. A trial moving from phase 2 to phase 3 or 4

            To complete this task, follow these steps:

            1. Carefully read through each news article you find on the Internet.
            2. Look for information related to clinical trials, focusing on:
               - Compound or medicine names
               - Trial phases (preclinical, phase 1, phase 2, phase 3, phase 4)
               - Indications (diseases or conditions being treated)
               - Sponsor names (companies or organizations developing the compound)
               - Dates of announcements or trial progressions
               - Positive results or outcomes from previous trial phases
            3. For each potential opportunity you identify, extract the following information:
               - Compound name
               - Indication
               - Sponsor name
               - $oid from json for the relevant opportunity as OID
               - Relevant dates
               - Details explaining why it's considered an opportunity (e.g., phase progression, positive results)
            4. Present each identified opportunity in the following format:
               <opportunity>
               <compound_name>Insert compound name here</compound_name>
               <indication>Insert indication here</indication>
               <sponsor>Insert sponsor name here</sponsor>
               <oid> OID </oid>
               <date>Insert relevant date here</date>
               <details>Insert details about why this is considered an opportunity</details>
               </opportunity>
            5. If you find multiple opportunities, list them in separate <opportunity> tags.
            6. If you don't find any clinical trial opportunities in the provided JSON data, respond with:
               <no_opportunities>No clinical trial opportunities identified in the provided news articles.</no_opportunities>
            7. After listing all opportunities (or stating that none were found), provide a brief summary of your findings in <summary> tags.

            Remember to focus only on the criteria specified for clinical trial opportunities and ensure that you're extracting accurate information from the news articles.
            """;

        String fullPrompt = String.format("<json_news_articles>\n%s\n</json_news_articles>\n\n%s", jsonNewsArticles, systemPrompt);


        MessageCreateParams createParams = MessageCreateParams.builder()
                .model(Model.CLAUDE_3_5_SONNET_LATEST)
                .maxTokens(4096)
                .addUserMessage(fullPrompt)
                .build();

        return client.messages().create(createParams).content().stream()
                .flatMap(contentBlock -> contentBlock.text().stream())
                .map(textBlock -> textBlock.text())
                .collect(Collectors.toList());
    }


}
