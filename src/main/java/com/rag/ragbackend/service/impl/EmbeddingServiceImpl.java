package com.rag.ragbackend.service.impl;

import com.rag.ragbackend.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Service;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 兼容多种返回类型的 EmbeddingService。
 * - 将 embeddingModel.embed(text) 的返回值解析为 List<Double>
 * - 打印实际返回类型，便于调试
 */
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final Logger logger = Logger.getLogger(EmbeddingServiceImpl.class.getName());

    private final OllamaEmbeddingModel embeddingModel;

    /**
     * 返回 List<Double>，适合存入向量库或做相似度计算
     */
    @Override
    public List<Double> embedText(String text) {
        Object resp = embeddingModel.embed(text); // 可能是多种类型

        if (resp == null) {
            return List.of();
        }

        logger.info(() -> "Embedding raw response class: " + resp.getClass().getName());

        List<Double> vec = extractVector(resp);
        logger.info(() -> "Embedding vector length: " + vec.size());
        return vec;
    }

    /**
     * 尝试将不同类型的 embedding 返回值转换为 List<Double>
     */
    @SuppressWarnings("unchecked")
    private List<Double> extractVector(Object obj) {
        // 1) 原生数组
        if (obj instanceof double[]) {
            double[] arr = (double[]) obj;
            List<Double> list = new ArrayList<>(arr.length);
            for (double v : arr) list.add(v);
            return list;
        }
        if (obj instanceof float[]) {
            float[] arr = (float[]) obj;
            List<Double> list = new ArrayList<>(arr.length);
            for (float v : arr) list.add((double) v);
            return list;
        }

        // 2) List<?>
        if (obj instanceof List<?>) {
            List<?> raw = (List<?>) obj;
            List<Double> out = new ArrayList<>(raw.size());
            for (Object e : raw) {
                if (e instanceof Number) {
                    out.add(((Number) e).doubleValue());
                } else {
                    try {
                        out.add(Double.parseDouble(e.toString()));
                    } catch (Exception ex) {
                        // 非数值，跳过或抛异常
                    }
                }
            }
            return out;
        }

        // 3) 常见 getter 名称，通过反射调用
        String[] candidateGetters = {
                "getValues", "getValue", "getEmbedding", "getEmbeddings", "getOutput", "getVector", "getData"
        };
        for (String getter : candidateGetters) {
            try {
                Method m = obj.getClass().getMethod(getter);
                Object val = m.invoke(obj);
                if (val != null) {
                    return extractVector(val); // 递归处理返回值
                }
            } catch (NoSuchMethodException ignored) {
                // 忽略，尝试下一个 getter
            } catch (Exception ex) {
                logger.warning(() -> "Invoke getter " + getter + " failed: " + ex.getMessage());
            }
        }

        // 4) toString 解析（兜底，不推荐）
        try {
            String s = obj.toString();
            // 如果字符串是 [x,y,z] 格式，可以简单解析
            if (s.startsWith("[") && s.endsWith("]")) {
                String inner = s.substring(1, s.length() - 1);
                String[] parts = inner.split(",");
                List<Double> out = new ArrayList<>(parts.length);
                for (String p : parts) {
                    try {
                        out.add(Double.parseDouble(p.trim()));
                    } catch (Exception e) {
                        // skip
                    }
                }
                if (!out.isEmpty()) return out;
            }
        } catch (Exception ignored) {
        }

        // 无法解析，抛出异常提示开发者
        throw new IllegalStateException("Unsupported embedding response type: " + obj.getClass().getName()
                + ". Inspect logs to see available methods/fields.");
    }
}
