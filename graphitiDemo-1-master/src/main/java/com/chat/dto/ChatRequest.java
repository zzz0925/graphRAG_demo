// ChatRequest.java
package com.chat.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ChatRequest {
    @NotBlank(message = "消息内容不能为空")
    private String message;

    private String sessionId;
}