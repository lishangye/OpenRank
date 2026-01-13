package com.openrank.openrank.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openrank.openrank.mapper.RepoMapper;
import com.openrank.openrank.model.Repo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * GitHub 仓库拉取与入库服务，便于定时任务和单次导入复用。
 */
@Component
public class GithubRepoImportService {

    private static final Logger log = LoggerFactory.getLogger(GithubRepoImportService.class);
    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");

    private final RestTemplate restTemplate;
    private final RepoMapper repoMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String githubToken;

    public GithubRepoImportService(RestTemplateBuilder builder,
                                   RepoMapper repoMapper,
                                   @Value("${github.token:}") String githubToken) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(20))
                .build();
        this.repoMapper = repoMapper;
        this.githubToken = githubToken;
    }

    /**
     * 根据 fullName 调用 GitHub API 获取仓库信息并入库。
     */
    public Repo importByFullName(String fullName) {
        if (fullName == null || !fullName.contains("/")) {
            throw new IllegalArgumentException("fullName 格式应为 owner/repo");
        }
        String[] parts = fullName.split("/", 2);
        String owner = parts[0];
        String repo = parts[1];
        GithubRepoInfo info = fetchRepoInfo(owner, repo);
        return upsertRepo(info.getOwner(), info.getName(), info.getFullName(), info.getDescription(), info.getStars(), "github,manual");
    }

    /**
     * 将批量 GitHub 条目入库（供定时任务使用）。
     */
    public void importItems(List<GithubRepoItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (GithubRepoItem item : items) {
            String owner = item.owner != null ? item.owner.login : null;
            String name = item.name;
            String fullName = item.fullName;
            if ((owner == null || owner.isBlank() || name == null || name.isBlank()) && fullName != null && fullName.contains("/")) {
                String[] parts = fullName.split("/", 2);
                if (owner == null || owner.isBlank()) owner = parts[0];
                if (name == null || name.isBlank()) name = parts[1];
            }
            if (owner == null || owner.isBlank() || name == null || name.isBlank()) {
                log.warn("跳过无效仓库记录: fullName={}, owner={}, name={}", fullName, owner, name);
                continue;
            }
            if (fullName == null || fullName.isBlank()) {
                fullName = owner + "/" + name;
            }
            upsertRepo(owner, name, fullName, item.description, item.stars, "github,auto");
        }
    }

    private Repo upsertRepo(String owner, String name, String fullName, String description, Long stars, String tags) {
        String period = LocalDate.now().withDayOfMonth(1).toString();
        Repo repo = new Repo();
        repo.setOwner(owner);
        repo.setRepo(name);
        repo.setFullName(fullName);
        repo.setDisplayName(fullName);
        repo.setDescription(description);
        repo.setTags(tags);
        repo.setStatus("production");
        repo.setPriority("P2");
        repo.setStars(stars);
        repo.setOpenrank(fetchLatestOpenrank(owner, name));
        repo.setPeriod(period);
        repo.setPeriodType("MONTH");
        repoMapper.upsert(repo);
        return repo;
    }

    private GithubRepoInfo fetchRepoInfo(String owner, String repo) {
        String url = String.format("https://api.github.com/repos/%s/%s", owner, repo);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");
        if (githubToken != null && !githubToken.isBlank()) {
            headers.set("Authorization", "token " + githubToken);
        }
        ResponseEntity<GithubRepoInfo> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), GithubRepoInfo.class);
        GithubRepoInfo body = resp.getBody();
        if (body == null) {
            throw new RuntimeException("GitHub 返回空数据");
        }
        return body;
    }

    /**
     * 从 OpenDigger 拉取最新 openrank 值，失败返回 0。
     */
    public Double fetchLatestOpenrank(String owner, String repo) {
        if (owner == null || repo == null || owner.isBlank() || repo.isBlank()) {
            return 0.0;
        }
        try {
            String url = String.format("https://oss.x-lab.info/open_digger/github/%s/%s/openrank.json", owner, repo);
            String body = restTemplate.getForObject(url, String.class);
            if (body == null || body.isBlank()) {
                return 0.0;
            }
            Map<String, Double> map = objectMapper.readValue(body, new com.fasterxml.jackson.core.type.TypeReference<>() {});
            return map.entrySet().stream()
                    .filter(e -> e.getKey().matches("\\d{4}-\\d{2}"))
                    .max(Comparator.comparing(e -> YearMonth.parse(e.getKey(), YM)))
                    .map(Map.Entry::getValue)
                    .orElse(0.0);
        } catch (Exception ex) {
            log.debug("获取 {} openrank 失败: {}", owner + "/" + repo, ex.getMessage());
            return 0.0;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GithubRepoItem {
        public String name;
        @JsonProperty("full_name")
        public String fullName;
        @JsonProperty("description")
        public String description;
        @JsonProperty("stargazers_count")
        public Long stars;
        public GithubOwner owner;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GithubOwner {
        public String login;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GithubRepoInfo {
        private String name;
        @JsonProperty("full_name")
        private String fullName;
        private String description;
        @JsonProperty("stargazers_count")
        private Long stars;
        private GithubOwner owner;

        public String getName() { return name; }
        public String getFullName() { return fullName; }
        public String getDescription() { return description; }
        public Long getStars() { return stars; }
        public String getOwner() { return owner == null ? null : owner.login; }
    }
}
