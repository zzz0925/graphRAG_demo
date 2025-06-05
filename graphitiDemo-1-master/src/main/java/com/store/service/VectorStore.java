package com.store.service;

import com.store.entiry.ChunkResult;
import com.store.entiry.EmbeddingResult;

import java.util.List;

public interface VectorStore {
    List<ChunkResult> chunk(String doc);
    List<EmbeddingResult> embedding(List<ChunkResult> chunks);
    void store(List<EmbeddingResult> embeddings);
}
