package com.rag.ragbackend.service.impl;

import com.rag.ragbackend.service.ChromaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChromaServiceImpl implements ChromaService {

    private final WebClient chromaClient;

    private final VectorStore vectorStore;

    private static final String TENANT = "default_tenant";
    private static final String DATABASE = "default_database";
    private static final String COLLECTION = "rag_collection";

    /** 创建集合（如果存在不会报错） */
    @Override
    public void createCollectionIfNotExists() {
        chromaClient.post()
                .uri("/tenants/{tenant}/databases/{db}/collections",
                        TENANT, DATABASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "name", COLLECTION
                ))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    // 已存在则忽略
                    return reactor.core.publisher.Mono.empty();
                })
                .block();

        log.info("创建集合成功。");
    }

    /** 插入向量 */
    @Override
    public void addEmbedding(String id, String text, List<Double> embedding) {
        log.info("添加向量 id={}, text={}, embedding={}", id, text, embedding);

//        vectorStore.add();

        chromaClient.post()
                .uri("/tenants/{tenant}/databases/{db}/collections/{collection}/add",
                        TENANT, DATABASE, COLLECTION)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "ids", List.of(id),
                        "embeddings", List.of(embedding)
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /** 查询相关向量（v2 标准结构） */
    @Override
    public List<Map<String, Object>> queryEmbedding(List<Double> embedding, int topK) {

        Map<String, Object> resp = chromaClient.post()
                .uri("/tenants/{tenant}/databases/{db}/collections/{collection}/query",
                        TENANT, DATABASE, COLLECTION)
                .bodyValue(Map.of(
                        "query_embeddings", List.of(embedding),
                        "n_results", topK
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (resp == null || !resp.containsKey("documents")) {
            return List.of();
        }

        return (List<Map<String, Object>>) resp.get("documents");
    }
}
