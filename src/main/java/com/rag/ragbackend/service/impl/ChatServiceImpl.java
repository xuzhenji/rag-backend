package com.rag.ragbackend.service.impl;

import com.rag.ragbackend.service.ChatService;
import com.rag.ragbackend.service.EmbeddingService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final EmbeddingService embeddingService;
    private final OllamaChatModel chatModel;

    // 内存向量库：id -> vector
    private final Map<String, List<Double>> vectorStore = new ConcurrentHashMap<>();

    // id -> 原始文本（用于返回 snippet / 来源）
    private final Map<String, String> docStore = new ConcurrentHashMap<>();

    // topK 默认值
    private final int DEFAULT_TOP_K = 3;

    @PostConstruct
    public void initSampleData() {
        // 如果需要，可以在这里预加载一些示例文档方便调试
        if (docStore.isEmpty()) {
            upsertDocument("doc1", "这是示例文档 A 的第一段内容，包含关于产品型号和安装步骤的说明。");
            upsertDocument("doc2", "示例文档 B 包含定价和退换货政策的详细信息。");
            upsertDocument("doc3", "示例文档 C 介绍了常见问题与故障排查步骤。");
        }
    }

    /**
     * 向内存向量库写入或更新文档
     *
     * @param id   文档 id
     * @param text 文档文本（将被用来生成 embedding）
     */
    @Override
    public void upsertDocument(String id, String text) {
        List<Double> vec = embeddingService.embedText(text);
        if (vec == null) vec = Collections.emptyList();
        vectorStore.put(id, vec);
        docStore.put(id, text);
    }

    /**
     * 主入口：接收用户问题，返回 LLM 生成的答案（RAG 流程）
     *
     * @param query 用户问题
     * @return LLM 生成的文本回答
     */
    @Override
    public String chat(String query) {
        // 1) 生成 query embedding
        List<Double> qVec = embeddingService.embedText(query);
        if (qVec == null || qVec.isEmpty()) {
            // 兜底：直接调用 LLM
            return safeCallModel(buildPrompt(Collections.emptyList(), query));
        }

        // 2) 在内存向量库检索 topK
        List<ScoredDoc> top = retrieveTopK(qVec, DEFAULT_TOP_K);

        // 3) 从 top 中提取文本片段
        List<String> snippets = top.stream()
                .map(sd -> String.format("[%s] %s", sd.getId(), sd.getText()))
                .collect(Collectors.toList());

        // 4) 构建 prompt，并调用模型
        String prompt = buildPrompt(snippets, query);
        return safeCallModel(prompt);
    }

    /**
     * 在 vectorStore 中用 cosine 相似度检索 top K
     */
    private List<ScoredDoc> retrieveTopK(List<Double> queryVec, int k) {
        PriorityQueue<ScoredDoc> pq = new PriorityQueue<>(Comparator.comparingDouble(ScoredDoc::getScore).reversed());

        for (Map.Entry<String, List<Double>> e : vectorStore.entrySet()) {
            String id = e.getKey();
            List<Double> vec = e.getValue();
            double score = cosineSimilarity(queryVec, vec);
            // 仅当向量存在且有意义时才加入
            if (!Double.isNaN(score) && score > 0) {
                String text = docStore.getOrDefault(id, "");
                pq.add(new ScoredDoc(id, text, score));
            }
        }

        List<ScoredDoc> out = new ArrayList<>(k);
        for (int i = 0; i < k && !pq.isEmpty(); i++) {
            out.add(pq.poll());
        }
        return out;
    }

    /**
     * 构建最终发送给 LLM 的 prompt（简单模板）
     */
    private String buildPrompt(List<String> snippets, String question) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个基于知识库的问答助手。请只基于下面检索到的片段来回答用户的问题，并在回答末尾标注来源 id（例如 [doc1]）。\n\n");
        if (snippets != null && !snippets.isEmpty()) {
            sb.append("检索片段：\n");
            for (String s : snippets) {
                sb.append(s).append("\n\n");
            }
        } else {
            sb.append("（未检索到相关片段，直接基于已有模型知识回答）\n\n");
        }
        sb.append("问题：").append(question).append("\n\n");
        sb.append("请给出简洁、清晰的答复：");
        return sb.toString();
    }

    /**
     * 调用模型并处理异常
     */
    private String safeCallModel(String prompt) {
        try {
            // OllamaChatModel 的 call 方法在你的版本中应当存在并返回字符串
            return chatModel.call(prompt);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "模型调用失败：" + ex.getMessage();
        }
    }

    /**
     * 计算两个向量的 cosine 相似度（double）
     */
    private double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null) return Double.NaN;
        int n = Math.min(a.size(), b.size());
        if (n == 0) return Double.NaN;

        double dot = 0.0;
        double na = 0.0;
        double nb = 0.0;
        for (int i = 0; i < n; i++) {
            double va = a.get(i);
            double vb = b.get(i);
            dot += va * vb;
            na += va * va;
            nb += vb * vb;
        }
        if (na == 0 || nb == 0) return Double.NaN;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    /**
     * 简单的带分数的文档结构
     */
    private static class ScoredDoc {
        private final String id;
        private final String text;
        private final double score;

        public ScoredDoc(String id, String text, double score) {
            this.id = id;
            this.text = text;
            this.score = score;
        }

        public String getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public double getScore() {
            return score;
        }
    }
}
