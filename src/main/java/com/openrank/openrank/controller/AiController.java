package com.openrank.openrank.controller;

import com.openrank.openrank.ai.AiAssistant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "LangChain4j", description = "基于 LangChain4j 的对话接口")
public class AiController {

    private final AiAssistant aiAssistant;

    public AiController(AiAssistant aiAssistant) {
        this.aiAssistant = aiAssistant;
    }

    @Operation(
            summary = "AI 对话",
            description = "调用 LangChain4j 聊天模型，返回文本回答。",
            requestBody = @RequestBody(
                    required = true,
                    description = "请求体：{ \"message\": \"你的问题\" }",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            responses = @ApiResponse(responseCode = "200", description = "返回回答文本", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@org.springframework.web.bind.annotation.RequestBody Map<String, String> body) {
        String msg = body == null ? "" : body.getOrDefault("message", "");
        String reply = msg == null || msg.isBlank() ? "" : aiAssistant.chat(msg);
        return ResponseEntity.ok(Map.of("answer", reply));
    }
}
