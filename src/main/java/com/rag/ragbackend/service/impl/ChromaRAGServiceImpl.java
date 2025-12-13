package com.rag.ragbackend.service.impl;

import com.rag.ragbackend.service.ChatService;
import com.rag.ragbackend.service.ChromaRAGService;
import com.rag.ragbackend.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2025-12-8 22:05
 * @Author by zjh
 */
@Service
@RequiredArgsConstructor
public class ChromaRAGServiceImpl implements ChromaRAGService {

    private final ChromaServiceImpl chromaService;
    private final EmbeddingService embeddingService;
    private final ChatService chatService;

    /** 核心 RAG 工作流 */
    public RAGResponse chatWithRAG(String question) {

        // 1) 为问题生成向量
        List<Double> qVec = embeddingService.embedText(question);

        // 2) 相似度检索
        List<?> docs = chromaService.queryEmbedding(qVec, 3);

        List<String> chunks = docs.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        // 3) 构造 Prompt
        StringBuilder context = new StringBuilder();
        for (String chunk : chunks) {
            context.append(chunk).append("\n");
        }

        String prompt = """
                基于以下资料回答问题，请勿编造信息：
                
                【知识库内容】
                %s
                
                【用户问题】
                %s
                """.formatted(context, question);

        // 4) 调用大模型
        String answer = chatService.chat(prompt);

        return new RAGResponse(answer, chunks);
    }

    public record RAGResponse(String answer, List<String> chunks) {}
}
