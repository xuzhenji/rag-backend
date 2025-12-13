package com.rag.ragbackend.controller;

import com.rag.ragbackend.service.DataUploadService;
import com.rag.ragbackend.utils.TextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@CrossOrigin
public class DataUploadController {

    private final DataUploadService uploadService;

    @PostMapping("/upload")
    public UploadResponse upload(@RequestParam("file") MultipartFile file) throws Exception {

        String filename = file.getOriginalFilename();
        if (filename == null) throw new RuntimeException("文件名为空");

        String text;

        if (filename.endsWith(".pdf")) {
            text = TextExtractor.extractPdf(file.getInputStream());
        } else if (filename.endsWith(".txt") || filename.endsWith(".md")) {
            text = TextExtractor.extractText(file.getInputStream());
        } else {
            throw new RuntimeException("仅支持 PDF / TXT / MD 文件");
        }

        int chunks = uploadService.indexDocument(text);

        return new UploadResponse("上传成功", chunks);
    }

    public record UploadResponse(String message, int chunks) {}
}
