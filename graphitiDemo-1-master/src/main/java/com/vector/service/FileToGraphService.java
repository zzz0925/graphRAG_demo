package com.vector.service;

import com.chat.config.promptConfig;
import com.chat.service.impl.GremlinProcessor;
import com.chat.service.impl.GremlinServiceImpl;
import com.chat.service.impl.ModelServiceImpl;

import com.store.entiry.ChunkResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileToGraphService {

    private final textChunk textChunk;
    private final ModelServiceImpl modelService;
    private final GremlinServiceImpl gremlinService;

    // 线程池用于并发处理
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 处理文件：分块 -> 提取实体关系 -> 存储到图数据库
     */
    public void processFileToGraph(String docId) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("开始处理文件到图数据库: {}", docId);

                // 1. 文件分块
                List<ChunkResult> chunks = textChunk.chunk(docId);
                if (chunks == null || chunks.isEmpty()) {
                    log.error("文件分块失败或为空: {}", docId);
                    return;
                }

                log.info("文件分块完成，共 {} 个块", chunks.size());

                // 2. 为每个块提取实体和关系
                int totalChunks = chunks.size();
                int processedChunks = 0;
                int failedChunks = 0;

                for (ChunkResult chunk : chunks) {
                    try {
                        processedChunks++;
                        log.info("处理第 {}/{} 个文本块", processedChunks, totalChunks);
                        // 调用模型提取实体和关系
                        String prompt = promptConfig.configPrompt(promptConfig.MERGE_Gremlin, chunk.getContent());
                        String modelResponse = modelService.generateResponse(prompt);

                        if (modelResponse == null || modelResponse.trim().isEmpty()) {
                            log.warn("模型返回为空，跳过该文本块");
                            continue;
                        }

                        // 处理模型返回的 Gremlin 语句
                        List<String> gremlinStatements = GremlinProcessor.processGremlinResponse(modelResponse);

                        if (gremlinStatements.isEmpty()) {
                            log.warn("未提取到有效的 Gremlin 语句，跳过该文本块");
                            continue;
                        }

                        // 执行 Gremlin 语句
                        List<String> failedStatements = gremlinService.executeBatchGremlin(gremlinStatements);

                        if (!failedStatements.isEmpty()) {
                            log.warn("文本块 {} 中有 {} 条语句执行失败", chunk.getChunkId(), failedStatements.size());
                        }

                        // 避免过于频繁的调用
                        Thread.sleep(100);

                    } catch (Exception e) {
                        failedChunks++;
                        log.error("处理文本块 {} 时发生错误: {}", chunk.getChunkId(), e.getMessage(), e);
                    }
                }

                log.info("文件处理完成: {} - 总块数: {}, 成功: {}, 失败: {}",
                        docId, totalChunks, (processedChunks - failedChunks), failedChunks);

            } catch (Exception e) {
                log.error("处理文件 {} 时发生严重错误: {}", docId, e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * 处理单个文本内容到图数据库
     */
    public boolean processSingleTextToGraph(String text) {
        try {
            log.info("处理单个文本到图数据库");

            // 调用模型提取实体和关系
            String prompt = promptConfig.configPrompt(promptConfig.MERGE_Gremlin, text);
            String modelResponse = modelService.generateResponse(prompt);

            if (modelResponse == null || modelResponse.trim().isEmpty()) {
                log.warn("模型返回为空");
                return false;
            }

            // 处理模型返回的 Gremlin 语句
            List<String> gremlinStatements = GremlinProcessor.processGremlinResponse(modelResponse);

            if (gremlinStatements.isEmpty()) {
                log.warn("未提取到有效的 Gremlin 语句");
                return false;
            }

            // 执行 Gremlin 语句
            List<String> failedStatements = gremlinService.executeBatchGremlin(gremlinStatements);

            boolean success = failedStatements.isEmpty();
            log.info("单个文本处理完成，成功: {}", success);

            return success;

        } catch (Exception e) {
            log.error("处理单个文本时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
