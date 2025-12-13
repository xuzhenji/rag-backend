package com.rag.ragbackend.service;

import com.rag.ragbackend.service.impl.ChromaRAGServiceImpl;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2025-12-8 22:05
 * @Author by zjh
 */
public interface ChromaRAGService {
    ChromaRAGServiceImpl.RAGResponse chatWithRAG(String question);
}
