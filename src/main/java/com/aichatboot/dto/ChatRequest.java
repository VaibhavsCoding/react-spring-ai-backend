package com.aichatboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ChatRequest {

    /**
     * If messages is provided, it's treated as a conversation (role/content pairs).
     * Otherwise we fallback to 'prompt'.
     */
    private List<ChatMessage> messages;

    private String prompt;

    @NotNull
    private Boolean stream = Boolean.FALSE;

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public Boolean getStream() { return stream; }
    public void setStream(Boolean stream) { this.stream = stream; }
}
