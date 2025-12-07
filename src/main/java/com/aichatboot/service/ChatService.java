package com.aichatboot.service;

import com.aichatboot.dto.ChatMessage;
import com.aichatboot.dto.ChatRequest;
import com.aichatboot.dto.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;

@Service
public class ChatService {

    private final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final int maxTokens;
    private final double temperature;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    public ChatService(
            WebClient openAiWebClient,
            ObjectMapper objectMapper,
            @Value("${spring.ai.openai.chat.options.model:gpt-4.1-mini}") String model,
            @Value("${spring.ai.openai.chat.options.max-tokens:1500}") int maxTokens,
            @Value("${spring.ai.openai.chat.options.temperature:0.2}") double temperature
    ) {
        this.webClient = openAiWebClient;
        this.objectMapper = objectMapper;
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
    }

    /**
     * Build OpenAI request body
     */
    private Map<String, Object> buildRequest(ChatRequest request, boolean stream) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);
        body.put("stream", stream);
        body.put("messages", prepareMessages(request));
        return body;
    }

    /**
     * Synchronous chat completion
     */
    public ChatResponse createCompletion(ChatRequest request) {
        try {
            log.info("Calling OpenAI sync API → model={}", model);

            String responseBody = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(buildRequest(request, false)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(60));

            log.debug("OpenAI response: {}", responseBody);

            if (responseBody == null || responseBody.isBlank()) {
                return new ChatResponse(UUID.randomUUID().toString(), "[[No response from OpenAI]]");
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");

            StringBuilder aggregated = new StringBuilder();
            if (choices.isArray()) {
                for (JsonNode c : choices) {
                    JsonNode message = c.path("message");
                    if (!message.isMissingNode()) {
                        aggregated.append(message.path("content").asText(""));
                    }
                }
            }

            return new ChatResponse(
                    root.path("id").asText(UUID.randomUUID().toString()),
                    aggregated.toString().trim()
            );

        } catch (Exception ex) {
            log.error("OpenAI sync request failed", ex);
            return new ChatResponse(
                    UUID.randomUUID().toString(),
                    "[[ERROR: " + ex.getMessage() + "]]"
            );
        }
    }

    /**
     * Streaming completion using SSE
     */
    public Flux<String> streamCompletion(ChatRequest request) {
        try {
            log.info("Calling OpenAI STREAM API → model={}", model);

            return webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(buildRequest(request, true)))
                    .retrieve()
                    .bodyToFlux(String.class)
                    .flatMap(this::extractTextFromSSE)
                    .doOnNext(chunk -> log.debug("SSE token: {}", chunk))
                    .onErrorResume(err -> {
                        log.error("SSE stream error", err);
                        return Flux.just("[[ERROR: " + err.getMessage() + "]]");
                    })
                    .timeout(Duration.ofSeconds(120), Flux.just("[[STREAM TIMEOUT]]"));

        } catch (Exception ex) {
            log.error("OpenAI streaming failed", ex);
            return Flux.just("[[ERROR: " + ex.getMessage() + "]]");
        }
    }

    /**
     * Parse SSE chunks safely
     */
    private Flux<String> extractTextFromSSE(String raw) {
        try {
            List<String> outputs = new ArrayList<>();
            String[] lines = raw.split("\n");

            for (String line : lines) {
                line = line.trim();

                if (line.equals("data: [DONE]") || line.equals("[DONE]")) continue;
                if (line.startsWith("data:")) line = line.substring(5).trim();
                if (line.isEmpty()) continue;

                JsonNode json = objectMapper.readTree(line);
                JsonNode choices = json.path("choices");

                if (choices.isArray()) {
                    for (JsonNode c : choices) {
                        JsonNode delta = c.path("delta");
                        String token = delta.path("content").asText(null);
                        if (token != null) {
                            outputs.add(token);
                        }
                    }
                }
            }
            return Flux.fromIterable(outputs);
        } catch (Exception e) {
            log.error("Failed to parse SSE line: {}", raw, e);
            return Flux.empty();
        }
    }

    /**
     * Convert frontend request into ChatGPT messages array
     */
    private List<Map<String, String>> prepareMessages(ChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content", "You are a helpful assistant. Provide concise answers."
        ));

        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            for (ChatMessage m : request.getMessages()) {
                messages.add(Map.of("role", m.getRole(), "content", m.getContent()));
            }
        } else {
            messages.add(Map.of("role", "user", "content", request.getPrompt()));
        }

        return messages;
    }
}
