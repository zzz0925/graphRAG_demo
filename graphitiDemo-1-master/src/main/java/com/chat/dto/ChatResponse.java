// ChatResponse.java
package com.chat.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String response;
    private String sessionId;
    private boolean success;
    private String error;

    public static ChatResponse success(String response, String sessionId) {
        return new ChatResponse(response, sessionId, true, null);
    }

    public static ChatResponse error(String error) {
        return new ChatResponse(null, null, false, error);
    }
}