package com.store.repository;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.entiry.ElasticVectorData;
import com.store.entiry.EmbeddingResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@AllArgsConstructor
public class VectorStorage {

    final ElasticsearchTemplate elasticsearchTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getCollectionName(){
        //演示效果使用，固定前缀+日期
        return "llm_action_rag_"+ DateUtil.format(Date.from(Instant.now()),"yyyyMMdd");
    }

    /**
     * 初始化向量数据库index
     * @param collectionName 名称
     * @param dim 维度
     */
    public boolean initCollection(String collectionName,int dim){
        log.info("collection:{}", collectionName);
        // 查看向量索引是否存在，此方法为固定默认索引字段
        IndexOperations indexOperations = elasticsearchTemplate.indexOps(IndexCoordinates.of(collectionName));
        if (!indexOperations.exists()) {
            // 索引不存在，直接创建
            log.info("index not exists,create");
            //创建es的结构，简化处理
            Document document = Document.from(this.elasticMapping(dim));
            // 创建
            indexOperations.create(new HashMap<>(), document);
            return true;
        }
        return true;
    }

    public void store(String collectionName,List<EmbeddingResult> embeddingResults){
        //保存向量
        log.info("save vector,collection:{},size:{}",collectionName, CollectionUtil.size(embeddingResults));

        List<IndexQuery> results = new ArrayList<>();
        for (EmbeddingResult embeddingResult : embeddingResults) {
            ElasticVectorData ele = new ElasticVectorData();
            ele.setVector(embeddingResult.getEmbedding());
            ele.setChunkId(embeddingResult.getRequestId());
            ele.setContent(embeddingResult.getPrompt());
            results.add(new IndexQueryBuilder().withObject(ele).build());
        }
        // 构建数据包
        List<IndexedObjectInformation> bulkedResult = elasticsearchTemplate.bulkIndex(results, IndexCoordinates.of(collectionName));
        int size = CollectionUtil.size(bulkedResult);
        log.info("保存向量成功-size:{}", size);
    }

    /**
     * 使用余弦相似性算法进行向量检索
     * @param collectionName 集合名称
     * @param vector 查询向量
     * @return 最相似的内容
     */
    public String retrieval(String collectionName, double[] vector) {
        return retrieval(collectionName, vector, 1);
    }

    /**
     * 使用余弦相似性算法进行向量检索
     * @param collectionName 集合名称
     * @param vector 查询向量
     * @param topK 返回前K个结果
     * @return 最相似的内容列表
     */
    public List<String> retrievalTopK(String collectionName, double[] vector, int topK) {
        try {
            log.info("开始向量检索, collection: {}, vector维度: {}, topK: {}",
                    collectionName, vector.length, topK);

            // 构建余弦相似性脚本查询的JSON
            String scriptSource = "cosineSimilarity(params.query_vector, 'vector') + 1.0";

            // 构建查询JSON字符串
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("{");
            queryBuilder.append("\"script_score\": {");
            queryBuilder.append("\"query\": { \"match_all\": {} },");
            queryBuilder.append("\"script\": {");
            queryBuilder.append("\"source\": \"").append(scriptSource).append("\",");
            queryBuilder.append("\"params\": {");
            queryBuilder.append("\"query_vector\": [");
            for (int i = 0; i < vector.length; i++) {
                queryBuilder.append(vector[i]);
                if (i < vector.length - 1) {
                    queryBuilder.append(",");
                }
            }
            queryBuilder.append("]");
            queryBuilder.append("}");
            queryBuilder.append("}");
            queryBuilder.append("}");
            queryBuilder.append("}");

            // 使用StringQuery
            org.springframework.data.elasticsearch.core.query.Query query =
                    new org.springframework.data.elasticsearch.core.query.StringQuery(queryBuilder.toString());

            // 设置分页
            query.setPageable(org.springframework.data.domain.PageRequest.of(0, topK));

            // 设置源字段过滤
            query.addSourceFilter(new org.springframework.data.elasticsearch.core.query.FetchSourceFilter(
                    new String[]{"content", "chunkId"},
                    new String[]{"vector"}
            ));

            // 执行搜索
            SearchHits<ElasticVectorData> searchHits = elasticsearchTemplate.search(
                    query,
                    ElasticVectorData.class,
                    IndexCoordinates.of(collectionName)
            );

            List<String> results = new ArrayList<>();

            if (searchHits.hasSearchHits()) {
                for (SearchHit<ElasticVectorData> hit : searchHits.getSearchHits()) {
                    ElasticVectorData data = hit.getContent();
                    if (data != null && data.getContent() != null) {
                        results.add(data.getContent());
                    }
                }
            }

            log.info("向量检索完成, 返回结果数量: {}", results.size());
            return results;

        } catch (Exception e) {
            log.error("向量检索发生错误, collection: {}, error: {}", collectionName, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 返回单个最相似的结果
     */
    public String retrieval(String collectionName, double[] vector, int topK) {
        List<String> results = retrievalTopK(collectionName, vector, topK);
        return results.isEmpty() ? "" : results.get(0);
    }

    /**
     * 使用Spring Data Elasticsearch的方式进行检索（备用方案）
     */
    public String retrievalWithSpringData(String collectionName, double[] vector) {
        try {
            log.info("开始向量检索(Spring Data方式), collection: {}, vector维度: {}",
                    collectionName, vector.length);

            // 构建余弦相似性脚本
            String scriptSource = "cosineSimilarity(params.query_vector, 'vector') + 1.0";
            Map<String, Object> params = new HashMap<>();
            params.put("query_vector", vector);

            // 创建脚本
            //Script script = new Script(ScriptType.INLINE, "painless", scriptSource, params);

            // 构建查询 - 使用传统方式
            org.springframework.data.elasticsearch.core.query.Query query =
                    new org.springframework.data.elasticsearch.core.query.StringQuery("{"
                            + "\"script_score\": {"
                            + "  \"query\": { \"match_all\": {} },"
                            + "  \"script\": {"
                            + "    \"source\": \"" + scriptSource + "\","
                            + "    \"params\": " + objectMapper.writeValueAsString(params)
                            + "  }"
                            + "}"
                            + "}");

            // 设置分页
            query.setPageable(org.springframework.data.domain.PageRequest.of(0, 1));

            // 执行搜索
            SearchHits<ElasticVectorData> searchHits = elasticsearchTemplate.search(
                    query,
                    ElasticVectorData.class,
                    IndexCoordinates.of(collectionName)
            );

            if (searchHits.hasSearchHits()) {
                SearchHit<ElasticVectorData> hit = searchHits.getSearchHits().get(0);
                ElasticVectorData data = hit.getContent();
                //log.info("找到最相似内容, score: {}, chunkId: {}", hit.getScore(), data.getChunkId());
                return data.getContent();
            } else {
                log.warn("未找到相似内容");
                return "";
            }

        } catch (Exception e) {
            log.error("向量检索失败, collection: {}, error: {}", collectionName, e.getMessage(), e);
            return "";
        }
    }

    private Map<String, Object> elasticMapping(int dims) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("_class", MapUtil.builder("type", "keyword").put("doc_values", "false").put("index", "false").build());
        properties.put("chunkId", MapUtil.builder("type", "keyword").build());
        properties.put("content", MapUtil.builder("type", "keyword").build());
        properties.put("docId", MapUtil.builder("type", "keyword").build());
        // 向量
        properties.put("vector", MapUtil.builder("type", "dense_vector").put("dims", Objects.toString(dims)).build());
        Map<String, Object> root = new HashMap<>();
        root.put("properties", properties);
        return root;
    }
}