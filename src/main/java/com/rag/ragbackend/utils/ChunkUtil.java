package com.rag.ragbackend.utils;

import java.util.ArrayList;
import java.util.List;

public class ChunkUtil {

    /** 按长度切 chunk，可调  */
    public static List<String> split(String text, int maxLen) {
        List<String> chunks = new ArrayList<>();
        text = text.trim();

        for (int i = 0; i < text.length(); i += maxLen) {
            int end = Math.min(text.length(), i + maxLen);
            chunks.add(text.substring(i, end));
        }
        return chunks;
    }
}
