package com.rag.ragbackend.service;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2025-12-8 22:02
 * @Author by zjh
 */
public interface ChromaService {
    void createCollectionIfNotExists();

    void addEmbedding(String id, String text, List<Double> embedding);

    List<Map<String, Object>> queryEmbedding(List<Double> embedding, int topK);
}
