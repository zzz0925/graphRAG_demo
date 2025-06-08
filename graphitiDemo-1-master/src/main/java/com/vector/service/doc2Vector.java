package com.vector.service;

import com.store.entiry.ChunkResult;
import com.store.entiry.EmbeddingResult;
import com.store.service.VectorStore;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class doc2Vector {
    private textChunk textChunk;
    private VectorStore vectorStore;
    public String docVector(String doc){
        log.info("docVector start...");
        List<ChunkResult> chunkResults = textChunk.chunk(doc);
        log.info("chunk complete...");
        if(null == chunkResults){
            log.error("chunk result is null");
            return null;
        }
        log.info("embedding start...");
        List<EmbeddingResult> embedding = vectorStore.embedding(chunkResults);
        log.info("embedding complete...");
        vectorStore.store(embedding);
        log.info("docVector complete...");
        return "finished docId:{}"+doc;
    }
}
