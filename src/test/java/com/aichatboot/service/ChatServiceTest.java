package com.aichatboot.service;

import com.aichatboot.dto.ChatRequest;
import com.aichatboot.dto.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ChatServiceTest {

    private WebClient webClient;
    private WebClient.RequestBodyUriSpec requestBody;
    private WebClient.RequestHeadersSpec<?> headersSpec;
    private WebClient.ResponseSpec responseSpec;

    private ChatService chatService;

    @BeforeEach
    void setUp() {

        webClient = mock(WebClient.class);
        requestBody = mock(WebClient.RequestBodyUriSpec.class);
        headersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBody);
        when(requestBody.uri(anyString())).thenReturn(requestBody);
        when(requestBody.contentType(any(MediaType.class))).thenReturn(requestBody);
        when(requestBody.accept(any())).thenReturn(requestBody);

        // THE FINAL FIX → correct overload
        when(requestBody.body(any(BodyInserter.class))).thenReturn(headersSpec);

        when(headersSpec.retrieve()).thenReturn(responseSpec);

        chatService = new ChatService(webClient, new ObjectMapper(),
                "gpt-4.1-mini", 50, 0.1);
    }

    @Test
    void testSyncCompletion() throws Exception {

        String openAiResponse = """
            {
              "id": "test-id",
              "choices": [
                { "message": { "content": "Hi there!" } }
              ]
            }
            """;

        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(openAiResponse));

        ChatRequest req = new ChatRequest();
        req.setPrompt("Hello!");

        ChatResponse result = chatService.createCompletion(req);

        assertEquals("Hi there!", result.getText());
        assertEquals("test-id", result.getId());
    }

    @Test
    void testStreamCompletion() {

        String sse = "data: {\"choices\":[{\"delta\":{\"content\":\"Hello!\"}}]}";

        // Streaming uses same body() signature → mock again
        when(requestBody.body(any(BodyInserter.class))).thenReturn(headersSpec);

        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(sse, "data: [DONE]"));

        ChatRequest request = new ChatRequest();
        request.setPrompt("test");

        StepVerifier.create(chatService.streamCompletion(request))
                .expectNext("Hello!")
                .verifyComplete();
    }
}
