package com.openrank.openrank.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.util.Map;

/**
 * 代理调用外部 Chat Agent 接口，返回 Markdown 文本。
 */
@Service
public class ChatAgentService {

    private static final Logger log = LoggerFactory.getLogger(ChatAgentService.class);

    private final RestTemplate restTemplate;
    private final String agentEndpoint;
    private final String agentOpenEndpoint;
    private final String chatMessageBase;
    private final String apiKey;

    public ChatAgentService(RestTemplateBuilder restTemplateBuilder,
                            @Value("${chat.agent.endpoint}") String agentEndpoint,
                            @Value("${chat.agent.open-endpoint}") String agentOpenEndpoint,
                            @Value("${chat.agent.chat-message-base:}") String chatMessageBase,
                            @Value("${chat.agent.api-key}") String apiKey) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
        this.agentEndpoint = agentEndpoint;
        this.agentOpenEndpoint = agentOpenEndpoint;
        this.chatMessageBase = chatMessageBase;
        this.apiKey = apiKey;
    }

    /**
     * 发送提问到外部 Agent 接口，返回 Markdown 文本。
     * 请求格式参考 chat/api-doc，默认以 JSON 方式提交 { "query": "xxx" }。
     */
    public String askAgent(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "";
        }
        String sessionId = openSession();
        if (sessionId == null || sessionId.isBlank()) {
            log.warn("未获取到会话 ID，无法调用外部 Agent");
            return "";
        }
        String chatUrl = buildChatUrl(sessionId);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            if (apiKey != null && !apiKey.isBlank()) {
                headers.set("Authorization", "Bearer " + apiKey);
            }
            // 使用有序 Map，填充服务端期望的字段
            Map<String, Object> payload = new java.util.LinkedHashMap<>();
            payload.put("message", prompt);
            payload.put("user_question", prompt);
            payload.put("question", prompt);
            payload.put("stream", false);
            payload.put("re_chat", false);
            payload.put("chat_record_id", sessionId);
            payload.put("image_list", java.util.Collections.emptyList());
            payload.put("document_list", java.util.Collections.emptyList());
            payload.put("audio_list", java.util.Collections.emptyList());
            payload.put("video_list", java.util.Collections.emptyList());
            payload.put("other_list", java.util.Collections.emptyList());
            payload.put("form_data", java.util.Collections.emptyMap());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> resp = restTemplate.exchange(chatUrl, HttpMethod.POST, entity, String.class);
            return resp.getBody() == null ? "" : resp.getBody();
        } catch (RestClientException ex) {
            log.warn("调用外部 Agent 失败: {}", ex.getMessage());
            return "";
        }
    }

    /**
     * 调用 open 接口获取会话 ID（需要 API Key）。
     */
    public String openSession() {
        if (agentOpenEndpoint == null || agentOpenEndpoint.isBlank()) {
            log.warn("chat.agent.open-endpoint 未配置");
            return "";
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            if (apiKey != null && !apiKey.isBlank()) {
                headers.set("Authorization", "Bearer " + apiKey);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> resp = restTemplate.exchange(agentOpenEndpoint, HttpMethod.GET, entity, Map.class);
            Map<?, ?> body = resp.getBody();
            if (body == null) return "";
            Object data = body.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object sid = dataMap.get("sessionId");
                if (sid == null) sid = dataMap.get("session_id");
                if (sid != null) return sid.toString();
            }
            Object direct = body.get("data");
            if (direct == null) direct = body.get("session_id");
            return direct == null ? "" : direct.toString();
        } catch (RestClientException ex) {
            log.warn("获取会话 ID 失败: {}", ex.getMessage());
            return "";
        }
    }

    private String buildChatUrl(String sessionId) {
        if (chatMessageBase != null && !chatMessageBase.isBlank()) {
            String base = chatMessageBase.endsWith("/") ? chatMessageBase.substring(0, chatMessageBase.length() - 1) : chatMessageBase;
            return base + "/" + sessionId;
        }
        // 兼容旧的 endpoint（如果配置了 c5xxx 直接调用）
        return agentEndpoint;
    }
}
