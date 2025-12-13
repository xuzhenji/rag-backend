package com.rag.ragbackend.service.impl;

import com.rag.ragbackend.service.ChromaService;
import com.rag.ragbackend.service.EmbedAndStoreService;
import com.rag.ragbackend.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2025-12-8 22:03
 * @Author by zjh
 */
@Service
@RequiredArgsConstructor
public class EmbedAndStoreServiceImpl implements EmbedAndStoreService {

    private final EmbeddingService embeddingService;
    private final ChromaService chromaService;

    /** 文本向量化 + 写入 Chroma */
    public void indexText(String id, String text) {
        var vec = embeddingService.embedText(text);
        chromaService.addEmbedding(id, text, vec);
    }
}
