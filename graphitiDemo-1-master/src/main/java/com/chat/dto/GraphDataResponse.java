// graphitiDemo/src/main/java/com/chat/dto/GraphDataResponse.java (或者在其他dto包下)
package com.chat.dto;

import java.util.List;
import java.util.Map;

public class GraphDataResponse {
    private List<Map<String, Object>> nodes;
    private List<Map<String, Object>> relationships;

    // Constructors, getters, setters
    public GraphDataResponse(List<Map<String, Object>> nodes, List<Map<String, Object>> relationships) {
        this.nodes = nodes;
        this.relationships = relationships;
    }

    public List<Map<String, Object>> getNodes() {
        return nodes;
    }

    public void setNodes(List<Map<String, Object>> nodes) {
        this.nodes = nodes;
    }

    public List<Map<String, Object>> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Map<String, Object>> relationships) {
        this.relationships = relationships;
    }
}