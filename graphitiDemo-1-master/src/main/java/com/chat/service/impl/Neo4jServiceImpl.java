// graphitiDemo/src/main/java/com/chat/service/Neo4jService.java
package com.chat.service.impl;

import com.chat.dto.GraphData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class Neo4jServiceImpl {

    private final Neo4jClient neo4jClient; // 注入 Neo4jClient

    // 构造函数注入 Neo4jClient
    public Neo4jServiceImpl(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public String queryGraph(String cypher) throws JsonProcessingException {
        List<Map<String, Object>> queryResults = (List<Map<String, Object>>)neo4jClient.query(cypher)
                .fetch()
                .all();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResult = objectMapper.writeValueAsString(queryResults);
        log.info("queryGraph result: " + jsonResult);
        return jsonResult;
    }
    public boolean createNode(String cypher) {
        neo4jClient.query(cypher)
                .fetch()
                .all();
        return true;
    }
    public GraphData getAllGraphData() {
        // 使用 Neo4jClient 执行 Cypher 查询
        List<Map<String, Object>> rawResults = (List<Map<String, Object>>) neo4jClient
                .query("MATCH (n)-[r]->(m) RETURN n, r, m") // Cypher 查询
                .fetch() // 开始获取结果
                .all(); // 获取所有结果作为 List<Map<String, Object>>

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> relationships = new ArrayList<>();

        Map<Long, Map<String, Object>> uniqueNodes = new HashMap<>();

        for (Map<String, Object> row : rawResults) {
            Node nodeN = (Node) row.get("n");
            Relationship relationshipR = (Relationship) row.get("r");
            Node nodeM = (Node) row.get("m");

            if (nodeN != null) {
                Map<String, Object> nodeMap = new HashMap<>();
                nodeMap.put("id", nodeN.id());
                nodeMap.put("labels", StreamSupport.stream(nodeN.labels().spliterator(), false).collect(Collectors.toList()));
                nodeMap.put("properties", nodeN.asMap());
                uniqueNodes.put(nodeN.id(), nodeMap);
            }

            if (nodeM != null) {
                Map<String, Object> nodeMap = new HashMap<>();
                nodeMap.put("id", nodeM.id());
                nodeMap.put("labels", StreamSupport.stream(nodeM.labels().spliterator(), false).collect(Collectors.toList()));
                nodeMap.put("properties", nodeM.asMap());
                uniqueNodes.put(nodeM.id(), nodeMap);
            }

            if (relationshipR != null) {
                Map<String, Object> relMap = new HashMap<>();
                relMap.put("id", relationshipR.id());
                relMap.put("type", relationshipR.type());
                relMap.put("startNode", relationshipR.startNodeId());
                relMap.put("endNode", relationshipR.endNodeId());
                relMap.put("properties", relationshipR.asMap());
                relationships.add(relMap);
            }
        }

        nodes.addAll(uniqueNodes.values());

        return new GraphData(nodes, relationships);
    }

    // 可以根据需要添加其他查询方法，例如根据 sessionId 获取图数据
    public Map<String, Object> getGraphDataBySessionId(String sessionId) {
        // 实现根据 sessionId 查询并转换数据
        // 这是一个更复杂的查询，可能需要专门的Cypher语句来获取特定会话的子图
        // 例如：MATCH (c:Conversation {sessionId: $sessionId})-[r*0..2]-(n) RETURN c,r,n
        // 这里只是一个占位符，需要根据实际Neo4j数据模型细化
        return new HashMap<>(); // 返回空map或实际数据
    }
}