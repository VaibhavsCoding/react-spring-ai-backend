package com.aichatboot.controller;

import com.aichatboot.dto.ChatRequest;
import com.aichatboot.dto.ChatResponse;
import com.aichatboot.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ChatService chatService;

    @Test
    void testSyncChat() {
        when(chatService.createCompletion(any(ChatRequest.class)))
                .thenReturn(new ChatResponse("id-123", "Hello back!"));

        ChatRequest request = new ChatRequest();
        request.setPrompt("Hello");

        webTestClient.post().uri("/api/v1/chat")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.text").isEqualTo("Hello back!")
                .jsonPath("$.id").isEqualTo("id-123");
    }

    @Test
    void testStreamChat() {

        when(chatService.streamCompletion(any(ChatRequest.class)))
                .thenReturn(Flux.just("token1", "token2"));

        ChatRequest request = new ChatRequest();
        request.setPrompt("Hi stream");

        webTestClient.post().uri("/api/v1/chat/stream")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBody(String.class)
                .consumeWith(res -> {
                    String body = res.getResponseBody();
                    assert body.contains("token1");
                    assert body.contains("token2");
                });
    }
}
