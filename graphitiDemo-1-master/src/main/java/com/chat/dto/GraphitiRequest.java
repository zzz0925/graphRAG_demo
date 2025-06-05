// graphitiDemo/src/main/java/com/chat/dto/GraphitiRequest.java
package com.chat.dto;

import lombok.Data; // 确保导入 Lombok 的 Data 注解
import java.util.List;

@Data // @Data 会自动生成 getter/setter/toString/equals/hashCode
public class GraphitiRequest {
    private String sessionId; // 对应Graphiti的conversationId
    private List<GraphitiMessage> messages;
    private String groupId; // 可选，用于分组对话

    // 构造函数，方便创建 GraphitiRequest 实例
    public GraphitiRequest(String sessionId, List<GraphitiMessage> messages) {
        this.sessionId = sessionId;
        this.messages = messages;
    }

    // 内部静态类 GraphitiMessage
    // 确保这个内部类被定义在这里
    @Data // 为内部类也添加 @Data 注解
    public static class GraphitiMessage {
        private String role;    // "user" 或 "assistant"
        private String content;
        private long timestamp; // 时间戳，毫秒
    }
}