package com.aichatboot.controller;

import com.aichatboot.dto.ChatRequest;
import com.aichatboot.dto.ChatResponse;
import com.aichatboot.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/chat")
@Validated
@CrossOrigin(origins = "http://localhost:5173") // Allow React dev server
public class ChatController {

    private final ChatService chatService;
    private final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Value("${openai.stream.enabled:true}")
    private boolean streamEnabled;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        logger.debug("Received synchronous chat request, stream={}", request.getStream());
        try {
            return chatService.createCompletion(request);
        } catch (Exception ex) {
            logger.error("Error during synchronous chat completion", ex);
            return new ChatResponse("ERROR-" + System.currentTimeMillis(),
                    "[[ERROR: " + ex.getMessage() + "]]");
        }
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@Valid @RequestBody ChatRequest request) {
        if (!streamEnabled) {
            return Flux.just(ServerSentEvent.builder("Streaming disabled on server").build());
        }
        Flux<String> stream = chatService.streamCompletion(request);
        if (stream == null) stream = Flux.just("[[NO DATA]]");

        return stream
                .map(token -> ServerSentEvent.builder(token).build())
                .doOnError(e -> logger.error("Error during streaming chat", e))
                .onErrorResume(e -> Flux.just(ServerSentEvent.builder("[[STREAM ERROR: " + e.getMessage() + "]]").build()));
    }
}
