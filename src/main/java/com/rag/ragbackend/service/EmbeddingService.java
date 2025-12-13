package com.rag.ragbackend.service;


import java.util.List;

/**
 * 兼容多种返回类型的 EmbeddingService。
 * - 将 embeddingModel.embed(text) 的返回值解析为 List<Double>
 * - 打印实际返回类型，便于调试
 */
public interface EmbeddingService {

    List<Double> embedText(String text);
}
