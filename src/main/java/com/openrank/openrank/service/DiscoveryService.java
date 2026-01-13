package com.openrank.openrank.service;

import com.openrank.openrank.model.ProjectView;
import com.openrank.openrank.model.RepoRanking;
import com.openrank.openrank.mapper.RepoRankingMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 自动发现/排行榜数据源：OpenDigger openrank 榜单 + GitHub 热门仓库。
 */
@Service
public class DiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryService.class);
    private static final String GH_SEARCH_URL = "https://api.github.com/search/repositories?q=stars:%3E5000&sort=stars&order=desc&per_page=%d";
    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final RepoRankingMapper repoRankingMapper;

    public DiscoveryService(RepoRankingMapper repoRankingMapper) {
        this.repoRankingMapper = repoRankingMapper;
    }

    /**
     * 仅从本地数据库 repo_ranking 获取排行榜（默认月榜，按 rank 排序）。
     */
    public List<ProjectView> topOpenrank(int limit) {
        return fromDb("MONTH", "rank", limit);
    }

    /**
     * GitHub 热门仓库，再结合 OpenDigger 指标（best effort）。
     */
    public List<ProjectView> topGithubWithMetrics(int limit) {
        limit = Math.min(Math.max(limit, 1), 50);
        List<ProjectView> list = fetchGithubTop(limit);
        return list;
    }

    public List<ProjectView> fromDbPublic(String periodType, String order, int limit) {
        List<ProjectView> primary = fromDb(periodType, order, limit);
        if (!primary.isEmpty()) {
            return primary;
        }
        // 如果指定周期类型无数据，尝试另一种周期（MONTH <-> WEEK）保证仍从数据库获取
        String fallback = "MONTH".equalsIgnoreCase(periodType) ? "WEEK" : "MONTH";
        return fromDb(fallback, order, limit);
    }

    // 不再拉取外部榜单，统一依赖数据库数据

    @SuppressWarnings("unchecked")
    private List<ProjectView> fetchGithubTop(int limit) {
        List<ProjectView> result = new ArrayList<>();
        try {
            String url = GH_SEARCH_URL.formatted(limit);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "openrank-discovery")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                log.warn("GitHub 搜索请求失败 HTTP {}", resp.statusCode());
                return result;
            }
            Map<String, Object> json = new com.fasterxml.jackson.databind.ObjectMapper().readValue(resp.body(), new com.fasterxml.jackson.core.type.TypeReference<>() {});
            List<Map<String, Object>> items = (List<Map<String, Object>>) json.getOrDefault("items", List.of());
            for (int i = 0; i < items.size() && i < limit; i++) {
                Map<String, Object> item = items.get(i);
                String fullName = String.valueOf(item.getOrDefault("full_name", ""));
                String description = Optional.ofNullable(item.get("description")).map(Object::toString).orElse("GitHub 热门仓库");
                long stars = ((Number) item.getOrDefault("stargazers_count", 0)).longValue();
                String owner = "";
                String repo = "";
                if (fullName.contains("/")) {
                    String[] parts = fullName.split("/", 2);
                    owner = parts[0];
                    repo = parts[1];
                }

                double openrankVal = 0.0;
                String trend = "指标获取失败";
                try {
                    Map<String, Double> openrank = fetchMetricFromOpenDigger(owner, repo, "openrank");
                    YearMonth latest = latestYearMonth(openrank).orElse(YearMonth.now());
                    YearMonth prev = latest.minusMonths(1);
                    double latestVal = openrank.getOrDefault(latest.format(YM), 0.0);
                    double prevVal = openrank.getOrDefault(prev.format(YM), 0.0);
                    openrankVal = latestVal;
                    trend = String.format("OpenRank %+.1f", latestVal - prevVal);
                } catch (Exception ex) {
                    log.warn("获取 {} OpenRank 失败: {}", fullName, ex.getMessage());
                }

                result.add(new ProjectView(
                        fullName,
                        fullName,
                        owner,
                        "-",
                        stars,
                        trend,
                        List.of("github", "auto"),
                        "production",
                        "P2",
                        description,
                        List.of(),
                        openrankVal
                ));
            }
        } catch (Exception e) {
            log.warn("解析 GitHub 榜单失败: {}", e.getMessage());
        }
        return result;
    }

    private Map<String, Double> fetchMetricFromOpenDigger(String owner, String repo, String metric) throws Exception {
        if (owner == null || repo == null || owner.isBlank() || repo.isBlank()) {
            throw new IllegalArgumentException("仓库路径为空");
        }
        String url = String.format("https://oss.x-lab.info/open_digger/github/%s/%s/%s.json", owner, repo, metric);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "openrank-discovery")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new RuntimeException("OpenDigger HTTP " + resp.statusCode());
        }
        return new com.fasterxml.jackson.databind.ObjectMapper().readValue(resp.body(), new com.fasterxml.jackson.core.type.TypeReference<>() {});
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        try {
            return Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Optional<YearMonth> latestYearMonth(Map<String, Double> metric) {
        return metric.keySet().stream()
                .filter(k -> k.matches("\\d{4}-\\d{2}"))
                .map(YearMonth::parse)
                .max(YearMonth::compareTo);
    }

    /**
     * 从 repo_ranking 表读取最新一期（按 period_type）排行榜。
     */
    private List<ProjectView> fromDb(String periodType, String order, int limit) {
        try {
            String latest = repoRankingMapper.findLatestPeriod(periodType);
            if (latest == null || latest.isBlank()) {
                return List.of();
            }
            List<RepoRanking> rows = repoRankingMapper.listByPeriodOrdered(periodType, latest, normalizeOrder(order), limit);
            if (rows == null || rows.isEmpty()) {
                return List.of();
            }
            List<ProjectView> result = new ArrayList<>();
            for (RepoRanking r : rows) {
                String repoPath = r.getOwner() + "/" + r.getRepo();
                List<String> tags = r.getTags() == null ? List.of() :
                        java.util.Arrays.stream(r.getTags().split(","))
                                .map(String::trim).filter(s -> !s.isEmpty()).toList();
                result.add(new ProjectView(
                        Optional.ofNullable(r.getDisplayName()).orElse(repoPath),
                        repoPath,
                        r.getOwner(),
                        r.getPeriod(),
                        Optional.ofNullable(r.getStars()).orElse(0L),
                        "DB 排行",
                        tags,
                        "production",
                        "P2",
                        Optional.ofNullable(r.getDescription()).orElse("来自 repo_ranking 表的排行数据"),
                        List.of(),
                        Optional.ofNullable(r.getOpenrank()).orElse(0.0)
                ));
            }
            return result;
        } catch (Exception e) {
            log.warn("从数据库读取排行失败: {}", e.getMessage());
            return List.of();
        }
    }

    private String normalizeOrder(String order) {
        return switch (order == null ? "" : order.toLowerCase()) {
            case "openrank" -> "openrank";
            case "stars" -> "stars";
            case "delta_stars", "deltastars" -> "delta_stars";
            case "attention" -> "attention";
            case "activity" -> "activity";
            default -> "rank";
        };
    }

    /**
     * 直接获取 repo 表全部仓库（按 openrank 降序），如果 openrank 为空则其次按 stars。
     */
    public List<ProjectView> allRepos(int limit) {
        limit = Math.min(Math.max(limit, 1), 500);
        try {
            List<RepoRanking> rows = repoRankingMapper.listAll(limit);
            if (rows == null || rows.isEmpty()) {
                return List.of();
            }
            List<ProjectView> result = new ArrayList<>();
            for (RepoRanking r : rows) {
                String repoPath = r.getOwner() + "/" + r.getRepo();
                List<String> tags = r.getTags() == null ? List.of() :
                        java.util.Arrays.stream(r.getTags().split(","))
                                .map(String::trim).filter(s -> !s.isEmpty()).toList();
                result.add(new ProjectView(
                        Optional.ofNullable(r.getDisplayName()).orElse(repoPath),
                        repoPath,
                        r.getOwner(),
                        Optional.ofNullable(r.getPeriod()).orElse("-"),
                        Optional.ofNullable(r.getStars()).orElse(0L),
                        "DB 全量",
                        tags,
                        "production",
                        "P2",
                        Optional.ofNullable(r.getDescription()).orElse("来自 repo 表的数据"),
                        List.of(),
                        Optional.ofNullable(r.getOpenrank()).orElse(0.0)
                ));
            }
            return result;
        } catch (Exception e) {
            log.warn("从数据库读取 repo 表失败: {}", e.getMessage());
            return List.of();
        }
    }
}
