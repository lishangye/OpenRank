package com.openrank.openrank.controller;

import com.openrank.openrank.service.ChatAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@Tag(name = "Agent 代理", description = "通过外部 chat 接口获取 Markdown 响应")
public class AgentController {

    private final ChatAgentService chatAgentService;

    public AgentController(ChatAgentService chatAgentService) {
        this.chatAgentService = chatAgentService;
    }

    @Operation(
            summary = "获取会话 ID",
            description = "调用 chat.agent.open-endpoint 获取会话 ID，可在调用 ask 前先获取。",
            responses = {
                    @ApiResponse(responseCode = "200", description = "返回会话 ID", content = @Content(schema = @Schema(implementation = Map.class)))
            }
    )
    @GetMapping("/session")
    public ResponseEntity<Map<String, String>> session() {
        String sessionId = chatAgentService.openSession();
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }

    @Operation(
            summary = "向外部 Agent 提问",
            description = "调用配置的 chat.agent.endpoint，将用户问题发送给 Agent，返回 Markdown 文本。",
            requestBody = @RequestBody(
                    required = true,
                    description = "请求体：{ \"query\": \"你的问题\" }",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Agent 返回的 Markdown 文本", content = @Content(schema = @Schema(implementation = Map.class)))
            }
    )
    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@org.springframework.web.bind.annotation.RequestBody Map<String, String> body) {
        String query = body == null ? null : body.get("query");
        String markdown = chatAgentService.askAgent(query);
        return ResponseEntity.ok(Map.of("markdown", markdown));
    }
}
