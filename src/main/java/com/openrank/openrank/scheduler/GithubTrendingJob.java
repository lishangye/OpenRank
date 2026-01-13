package com.openrank.openrank.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openrank.openrank.service.GithubRepoImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * 定时从 GitHub 获取热门仓库，写入 repo 表。
 */
@Component
public class GithubTrendingJob {

    private static final Logger log = LoggerFactory.getLogger(GithubTrendingJob.class);

    private final RestTemplate restTemplate;
    private final GithubRepoImportService importService;
    private final String token;
    private final int starsThreshold;
    private final int perPage;
    private final int maxPages;

    public GithubTrendingJob(RestTemplateBuilder builder,
                             GithubRepoImportService importService,
                             @Value("${github.token:}") String token,
                             @Value("${github.stars-threshold:5000}") int starsThreshold,
                             @Value("${github.per-page:50}") int perPage,
                             @Value("${github.max-pages:2}") int maxPages) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(20))
                .build();
        this.importService = importService;
        this.token = token;
        this.starsThreshold = starsThreshold;
        this.perPage = perPage;
        this.maxPages = maxPages;
    }

    @Scheduled(cron = "${github.sync-cron:0 0 * * * *}")
    public void fetchTrending() {
        if (token == null || token.isBlank()) {
            log.warn("GitHub token 未配置，跳过同步");
            return;
        }
        log.info("开始同步 GitHub 热门仓库...");
        try {
            for (int page = 1; page <= maxPages; page++) {
                String url = String.format("https://api.github.com/search/repositories?q=stars:>%d&sort=stars&order=desc&per_page=%d&page=%d",
                        starsThreshold, perPage, page);
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "token " + token);
                headers.set("Accept", "application/vnd.github+json");
                ResponseEntity<GithubSearchResp> resp = restTemplate.exchange(
                        url, HttpMethod.GET, new HttpEntity<>(headers), GithubSearchResp.class);
                GithubSearchResp body = resp.getBody();
                List<GithubRepoItem> items = body == null ? Collections.emptyList() : body.items;
                if (items == null || items.isEmpty()) {
                    break;
                }
                importService.importItems(mapItems(items));
                log.info("已同步第 {} 页，条数 {}", page, items.size());
            }
        } catch (Exception e) {
            log.warn("同步 GitHub 热门仓库失败: {}", e.getMessage());
        }
    }

    /**
     * 手动触发同步，可用于调试。
     *
     * @param limitPages 手动限制页数，null/小于1则使用配置的 maxPages
     */
    public void triggerSync(Integer limitPages) {
        int pages = (limitPages != null && limitPages > 0) ? limitPages : maxPages;
        log.info("手动触发 GitHub 热门仓库同步，页数：{}", pages);
        try {
            for (int page = 1; page <= pages; page++) {
                String url = String.format("https://api.github.com/search/repositories?q=stars:>%d&sort=stars&order=desc&per_page=%d&page=%d",
                        starsThreshold, perPage, page);
                HttpHeaders headers = new HttpHeaders();
                if (token != null && !token.isBlank()) {
                    headers.set("Authorization", "token " + token);
                }
                headers.set("Accept", "application/vnd.github+json");
                ResponseEntity<GithubSearchResp> resp = restTemplate.exchange(
                        url, HttpMethod.GET, new HttpEntity<>(headers), GithubSearchResp.class);
                GithubSearchResp body = resp.getBody();
                List<GithubRepoItem> items = body == null ? Collections.emptyList() : body.items;
                if (items == null || items.isEmpty()) {
                    break;
                }
                importService.importItems(mapItems(items));
                log.info("手动同步第{} 页，条数 {}", page, items.size());
            }
        } catch (Exception e) {
            log.warn("手动同步失败: {}", e.getMessage());
        }
    }

    private List<com.openrank.openrank.service.GithubRepoImportService.GithubRepoItem> mapItems(List<GithubRepoItem> items) {
        return items.stream().map(i -> {
            com.openrank.openrank.service.GithubRepoImportService.GithubRepoItem t = new com.openrank.openrank.service.GithubRepoImportService.GithubRepoItem();
            t.name = i.name;
            t.fullName = i.fullName;
            t.description = i.description;
            t.stars = i.stars;
            if (i.owner != null) {
                com.openrank.openrank.service.GithubRepoImportService.GithubOwner owner = new com.openrank.openrank.service.GithubRepoImportService.GithubOwner();
                owner.login = i.owner.login;
                t.owner = owner;
            }
            return t;
        }).toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GithubSearchResp {
        @JsonProperty("items")
        List<GithubRepoItem> items;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GithubRepoItem {
        String name;
        @JsonProperty("full_name")
        String fullName;
        @JsonProperty("description")
        String description;
        @JsonProperty("stargazers_count")
        Long stars;
        GithubOwner owner;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GithubOwner {
        String login;
    }
}
