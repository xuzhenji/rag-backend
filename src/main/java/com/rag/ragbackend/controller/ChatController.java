package com.rag.ragbackend.controller;

import com.rag.ragbackend.service.ChatService;
import com.rag.ragbackend.service.ChromaRAGService;
import com.rag.ragbackend.service.impl.ChromaRAGServiceImpl;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 允许跨域，前端可正常访问
public class ChatController {

    private final ChatService chatService;

    private final ChromaRAGService ragService;


    /**
     * 前端调用此接口进行对话
     */
    @PostMapping("/rag")
    public String chat(@RequestBody ChatRequest request) {
        String chat = chatService.chat(request.getMessage());
        log.info("返回值={}", chat);
        return chat;
    }


    @PostMapping("/ragDb")
    public ChromaRAGServiceImpl.RAGResponse ragDb(@RequestBody Question req) {
        return ragService.chatWithRAG(req.message);
    }

    @Data
    public static class Question {
        public String message;
    }

    public record RAGResponse(String answer, Object chunks) {}




    /**
     * 接收前端 JSON 格式 {"message": "..."}
     */
    public static class ChatRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
