package com.rag.ragbackend.service.impl;

import com.rag.ragbackend.service.DataUploadService;
import com.rag.ragbackend.service.EmbeddingService;
import com.rag.ragbackend.utils.ChunkUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2025-12-9 21:23
 * @Author by zjh
 */
@Service
@RequiredArgsConstructor
public class DataUploadServiceImpl implements DataUploadService {
    private final EmbeddingService embeddingService;
    private final ChromaServiceImpl chromaService;

    /** 将一整个文档写入 Chroma（自动切 chunk） */
    @Override
    public int indexDocument(String text) {
//        chromaService.createCollectionIfNotExists();
        List<String> chunks = ChunkUtil.split(text, 400);
        int count = 0;
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);

            var vec = embeddingService.embedText(chunk);

            // id 不能重复
            String id = "doc_" + System.currentTimeMillis() + "_" + i;

            chromaService.addEmbedding(id, chunk, vec);

            count++;
        }
        return count;
    }
}
