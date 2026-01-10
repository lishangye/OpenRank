package com.openrank.openrank.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openrank.openrank.model.ProjectView;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 集成测试：直接请求 GitHub + OpenDigger 获取榜单。
 * 说明：
 *  - 依赖外网，可能受限于 GitHub 速率限制（未带 token 时每小时 60 次）。
 *  - 如遇 403/超时，请配置环境变量 GITHUB_TOKEN 并在 DiscoveryService 中增加 Authorization 头，或降低调用频率。
 */
@SpringBootTest
@Tag("integration")
class DiscoveryServiceTest {

    @Autowired
    private DiscoveryService discoveryService;
    private final ObjectMapper mapper = new ObjectMapper();

    // @Test
    // void topGithubWithMetrics_realData() {
    //     List<ProjectView> list = discoveryService.topGithubWithMetrics(5);
    //     assertNotNull(list, "返回结果不应为 null");
    //     // 若被限流，允许空列表；若成功获取则至少包含一条
    //     if (list.isEmpty()) {
    //         System.out.println("GitHub/OpenDigger 可能被限流，返回空列表");
    //     } else {
    //         assertFalse(list.get(0).repo().isEmpty(), "repo 应有值");
    //         System.out.println("Top repo: " + list.get(0).repo());
    //     }
    // }

    /**
     * 直接请求 GitHub 搜索 API，验证能返回 200 以及 items 字段存在。
     * 如果遇到 403/429（速率限制），则跳过测试。
     */
    @Test
    void githubSearchApi_shouldReturnItems() throws Exception {
        String url = "https://api.github.com/search/repositories?q=stars:%3E5000&sort=stars&order=desc&per_page=3";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "openrank-test")
                .timeout(java.time.Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 403 || resp.statusCode() == 429) {
            Assumptions.assumeTrue(false, "GitHub API rate limited, status " + resp.statusCode());
            return;
        }

        Assumptions.assumeTrue(resp.statusCode() == 200, "GitHub API unexpected status " + resp.statusCode());

        Map<String, Object> json = mapper.readValue(resp.body(), Map.class);
        Object items = json.get("items");
        assertNotNull(items, "items 应存在");
        List<?> list = (List<?>) items;
        list.stream().limit(3).forEach(it -> {
            if (it instanceof Map<?,?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) map;
                System.out.println("Found repo: " + m.getOrDefault("full_name", "unknown"));
            }
        });
    }
    

}
