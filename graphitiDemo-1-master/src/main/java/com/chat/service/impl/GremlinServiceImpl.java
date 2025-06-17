package com.chat.service.impl;

import com.chat.dto.GraphData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.transform.Undefined;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GremlinServiceImpl {

    @Autowired
    private Client gremlinClient;

    /**
     * 执行单条 Gremlin 语句
     */
    public boolean executeSingleGremlin(String gremlinStatement) {
        try {
            log.info("执行 Gremlin 语句: {}", gremlinStatement);
            ResultSet resultSet = gremlinClient.submit(gremlinStatement.trim());
            resultSet.all().join(); // 等待执行完成
            log.info("Gremlin 语句执行成功");
            return true;
        } catch (Exception e) {
            log.error("执行 Gremlin 语句失败: {}, 错误: {}", gremlinStatement, e.getMessage());
            return false;
        }
    }
    /**
     * 批量执行多条 Gremlin 语句
     */
    public List<String> executeBatchGremlin(List<String> gremlinStatements) {
        List<String> failedStatements = new ArrayList<>();

        for (String statement : gremlinStatements) {
            if (!statement.trim().isEmpty()) {
                boolean success = executeSingleGremlin(statement);
                if (!success) {
                    failedStatements.add(statement);
                }
            }
        }

        log.info("批量执行完成，失败语句数量: {}", failedStatements.size());
        return failedStatements;
    }
    public String GremlinQuery(String gremlin) {
        long start = System.currentTimeMillis();
        ResultSet submit = gremlinClient.submit(gremlin);
        long end = System.currentTimeMillis();
        log.info("Gremlin Query: {}, Gremlin Query Time: {} ms", gremlin,end - start);
        return resultSetToJsonString(submit);
    }
    public String resultSetToJsonString(ResultSet resultSet) {
        try {
            List<Result> results = resultSet.all().join();
            List<Map<String,Object>> maps = new ArrayList<>();
            if (!results.isEmpty()) {
                for (Result result : results) {
                    Map<String, Object> map = result.get(Map.class);
                    maps.add(map);
                }
            }
            return maps.toString();
        } catch (Exception e) {
            log.error("转换 ResultSet 到 string 失败: {}", e.getMessage());
            return "[]";
        }
    }
    public GraphData getAllGraphData() {
        // 查询 100 个顶点
        String vertexQuery = "g.V().limit(100).valueMap(true)";
        ResultSet vertexResults = gremlinClient.submit(vertexQuery);

        List<String> vertexIds = new ArrayList<>();
        List<Map<String, Object>> nodes = vertexResults.all().join().stream()
                .map(Result::getObject)
                .map(obj -> (Map<?, ?>) obj)
                .map(this::convertGremlinMapToStandardMap)
                .collect(Collectors.toList());

        nodes.forEach(node -> vertexIds.add(String.valueOf(node.get("id"))));

        // Step 2: 根据这些顶点 ID 查询连接的边
        String edgeQuery = String.format(
                "g.V(%s).bothE().where(otherV().hasId(%s)).dedup().project('id', 'label', 'inV', 'outV', 'props') "
                        + ".by(id).by(label).by(inV().id()).by(outV().id()).by(valueMap(true))",
                vertexIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(",")),
                vertexIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(","))
        );
        ResultSet edgeResults = gremlinClient.submit(edgeQuery);

        List<Map<String, Object>> relationships = edgeResults.all().join().stream()
                .map(Result::getObject)
                .map(obj -> (Map<?, ?>) obj)
                .map(this::convertGremlinMapToStandardMap)
                .collect(Collectors.toList());


        return new GraphData(nodes, relationships);
    }

    // 清洗 Gremlin Map 工具方法
    private Map<String, Object> convertGremlinMapToStandardMap(Map<?, ?> gremlinMap) {
        Map<String, Object> cleanedMap = new HashMap<>();

        for (Map.Entry<?, ?> entry : gremlinMap.entrySet()) {
            // 转换 key 成 String
            String key = entry.getKey() instanceof Enum ? entry.getKey().toString() : entry.getKey().toString();

            // 处理 value，如果是集合就取第一个元素或保留列表
            Object value = entry.getValue();
            if (value instanceof List<?>) {
                List<?> listValue = (List<?>) value;
                if (!listValue.isEmpty()) {
                    // 如果是单个值的列表，可以只取第一个元素（根据需要）
                    // 否则保留完整列表
                    if (listValue.size() == 1) {
                        value = convertGremlinValue(listValue.get(0));
                    } else {
                        value = listValue.stream().map(this::convertGremlinValue).collect(Collectors.toList());
                    }
                }
            } else {
                value = convertGremlinValue(value);
            }

            cleanedMap.put(key, value);
        }

        return cleanedMap;
    }

    // 统一转换单个值为标准类型
    private Object convertGremlinValue(Object value) {
        if (value instanceof Enum) {
            return value.toString();
        } else if (value instanceof Number || value instanceof String || value instanceof Boolean) {
            return value;
        } else if (value != null) {
            return value.toString();  // 最后兜底转成字符串
        } else {
            return null;
        }
    }

}
