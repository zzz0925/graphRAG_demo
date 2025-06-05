// graphitiDemo/src/main/java/com/chat/dto/GraphData.java
package com.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphData {
    private List<Map<String, Object>> nodes;
    private List<Map<String, Object>> relationships;
}