package com.rag.ragbackend.service.impl;

import com.rag.ragbackend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.*;


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
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final OllamaChatModel chatModel;

    private final StreamingChatModel streamingChatModel;

    @Override
    public Flux<String> streamChatRag(String query) {
        try {
            Flux<String> stream = chatModel.stream(query);
            return stream;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void CompressionChat() {

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
}
