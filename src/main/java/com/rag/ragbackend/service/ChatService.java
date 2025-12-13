package com.rag.ragbackend.service;



/**
 * ChatService - 最小可用的内存 RAG 流程实现
 *
 * 流程：
 * 1. 将用户 query 通过 EmbeddingService 生成向量
 * 2. 在内存向量库中做相似度检索（cosine）
 * 3. 将 topK 检索到的片段拼接到 prompt 中，调用 OllamaChatModel 生成最终回答
 *
 * 注意：
 * - 这是演示/开发阶段用的实现，推荐在生产中替换为 Milvus / PGVector / Chroma 等向量库
 */
public interface ChatService {

    void upsertDocument(String id, String text);

    String chat(String query);

}
