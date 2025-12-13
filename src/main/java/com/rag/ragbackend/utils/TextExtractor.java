package com.rag.ragbackend.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TextExtractor {

    /** 读取 PDF → 提取文本 */
    public static String extractPdf(InputStream in) {
        try (PDDocument document = PDDocument.load(in)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            throw new RuntimeException("PDF解析失败", e);
        }
    }

    /** 读取 txt/md → 字符串 */
    public static String extractText(InputStream in) {
        try {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("文本文件读取失败", e);
        }
    }
}
