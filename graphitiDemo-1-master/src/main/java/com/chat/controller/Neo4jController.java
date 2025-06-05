// graphitiDemo/src/main/java/com/chat/controller/Neo4jController.java
package com.chat.controller;

import com.chat.dto.GraphData;
import com.chat.service.impl.Neo4jServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/neo4j")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 允许前端跨域访问
@Slf4j
public class Neo4jController {

    private final Neo4jServiceImpl neo4JServiceImpl;

    @GetMapping("/graph-data")
    public ResponseEntity<GraphData> getGraphData() {
        log.info("Fetching all Neo4j graph data.");
        GraphData graphData = neo4JServiceImpl.getAllGraphData();
        return ResponseEntity.ok(graphData);
    }

    @GetMapping("/graph-data/{sessionId}")
    public ResponseEntity<Map<String, Object>> getGraphDataBySession(@PathVariable String sessionId) {
        log.info("Fetching Neo4j graph data for session: {}", sessionId);
        Map<String, Object> graphData = neo4JServiceImpl.getGraphDataBySessionId(sessionId);
        if (graphData.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(graphData);
    }
}