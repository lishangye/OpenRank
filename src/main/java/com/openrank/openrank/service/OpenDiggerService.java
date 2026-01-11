package com.openrank.openrank.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openrank.openrank.model.ProjectView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OpenDiggerService {

    private static final Logger log = LoggerFactory.getLogger(OpenDiggerService.class);
    private static final String BASE_URL = "https://oss.x-lab.info/open_digger/github";
    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final String CACHE_KEY = "opendigger:projects";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final String METRIC_CACHE_KEY = "opendigger:metric:%s:%s";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StringRedisTemplate redisTemplate;

    private final List<RepoConfig> repos = List.of(
            new RepoConfig(
                    "pytorch",
                    "pytorch",
                    "PyTorch",
                    "PyTorch Team",
                    "主流深度学习框架，关注分布式训练与服务化。",
                    List.of("ai", "mlops", "training"),
                    "production",
                    "P1",
                    List.of("关注 nightly 稳定性", "整理近期 issue 反馈")
            ),
            new RepoConfig(
                    "microsoft",
                    "vscode",
                    "VS Code",
                    "Microsoft",
                    "最流行的开源编辑器之一，扩展生态活跃。",
                    List.of("editor", "devtools"),
                    "production",
                    "P1",
                    List.of("跟进稳定版迭代", "社区高频插件联动")
            ),
            new RepoConfig(
                    "vercel",
                    "next.js",
                    "Next.js",
                    "Vercel",
                    "React 全栈框架，重点关注 App Router 与边缘渲染。",
                    List.of("framework", "web", "edge"),
                    "beta",
                    "P1",
                    List.of("观察 App Router 反馈", "补充性能对比案例")
            ),
            new RepoConfig(
                    "apache",
                    "spark",
                    "Apache Spark",
                    "Apache",
                    "大数据计算核心引擎，适合流批一体场景。",
                    List.of("data", "infra"),
                    "production",
                    "P2",
                    List.of("回访核心贡献者", "更新发行版说明")
            ),
            new RepoConfig(
                    "apache",
                    "flink",
                    "Apache Flink",
                    "Apache",
                    "流式/批处理统一的分布式计算引擎。",
                    List.of("streaming", "infra"),
                    "beta",
                    "P2",
                    List.of("整理 2.0 新特性", "补充算子示例")
            ),
            new RepoConfig(
                    "X-lab2017",
                    "open-digger",
                    "OpenDigger",
                    "X-lab",
                    "开源数据分析平台，提供开放的开源指标数据。",
                    List.of("analytics", "data"),
                    "lab",
                    "P2",
                    List.of("监控数据产出延迟", "准备下一版开放接口")
            )
    );

    public OpenDiggerService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public List<ProjectView> fetchProjects() {
        List<ProjectView> cached = readCache();
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        List<ProjectView> fresh = repos.stream()
                .map(this::toProjectView)
                .toList();
        writeCache(fresh);
        return fresh;
    }

    public RepoConfig findConfig(String repoPath) {
        return repos.stream().filter(r -> r.repoPath().equalsIgnoreCase(repoPath)).findFirst().orElse(null);
    }

    public Map<String, Object> metricSeries(String repoPath) {
        RepoConfig config = findConfig(repoPath);
        if (config == null) {
            config = fallbackConfig(repoPath);
        }
        if (config == null) {
            log.warn("指标请求未知仓库: {}", repoPath);
            return Map.of("labels", List.of(), "openrank", List.of(), "stars", List.of(), "message", "未收录的仓库");
        }
        try {
            Map<String, Double> openrank = fetchMetricWithCache(config, "openrank");
            Map<String, Double> stars = fetchMetricWithCache(config, "stars");
            // 以指标的最新月份为基准回溯 12 个月，避免本地时间超出数据覆盖范围导致全 0
            YearMonth latest = latestYearMonth(openrank).orElse(YearMonth.now());
            List<String> labels = lastMonths(latest, 12);
            List<Double> openrankValues = labels.stream().map(m -> openrank.getOrDefault(m, 0.0)).toList();
            List<Double> starValues = labels.stream().map(m -> stars.getOrDefault(m, 0.0)).toList();
            return Map.of(
                    "labels", labels,
                    "openrank", openrankValues,
                    "stars", starValues,
                    "message", "ok"
            );
        } catch (Exception e) {
            log.warn("拉取指标失败 {}: {}", repoPath, e.getMessage());
            return Map.of("labels", List.of(), "openrank", List.of(), "stars", List.of(), "message", "加载失败");
        }
    }

    /**
     * 允许未预置的 repo 直接用 owner/repo 访问指标。
     */
    private RepoConfig fallbackConfig(String repoPath) {
        if (repoPath == null || !repoPath.contains("/")) {
            return null;
        }
        String[] parts = repoPath.split("/", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            return null;
        }
        String owner = parts[0].trim();
        String repo = parts[1].trim();
        log.info("指标请求使用动态仓库配置: {}", repoPath);
        return new RepoConfig(
                owner,
                repo,
                repoPath,
                owner,
                "动态仓库",
                List.of(),
                "production",
                "P2",
                List.of()
        );
    }

    private ProjectView toProjectView(RepoConfig config) {
        try {
            Map<String, Double> openrank = fetchMetricWithCache(config, "openrank");
            Map<String, Double> stars = fetchMetricWithCache(config, "stars");

            YearMonth latest = latestYearMonth(openrank).orElse(YearMonth.now().minusMonths(1));
            YearMonth previous = latest.minusMonths(1);

            double openrankLatest = metricValue(openrank, latest);
            double openrankDelta = openrankLatest - metricValue(openrank, previous);

            double starsLatest = metricValue(stars, latest);
            double starsDelta = starsLatest - metricValue(stars, previous);

            String trend = String.format("OpenRank %+.1f · Stars %+.0f", openrankDelta, starsDelta);

            return new ProjectView(
                    config.displayName(),
                    config.repoPath(),
                    config.maintainer(),
                    latest.toString(),
                    Math.round(starsLatest),
                    trend,
                    config.tags(),
                    config.status(),
                    config.priority(),
                    config.description(),
                    config.tasks(),
                    roundToSingleDecimal(openrankLatest)
            );
        } catch (Exception ex) {
            log.warn("无法从 OpenDigger 获取 {}: {}", config.repoPath(), ex.getMessage());
            return new ProjectView(
                    config.displayName(),
                    config.repoPath(),
                    config.maintainer(),
                    "数据暂缺",
                    0,
                    "数据拉取失败",
                    config.tags(),
                    config.status(),
                    config.priority(),
                    config.description(),
                    config.tasks(),
                    0.0
            );
        }
    }

    private List<ProjectView> readCache() {
        try {
            String json = redisTemplate.opsForValue().get(CACHE_KEY);
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("读取 OpenDigger 缓存失败: {}", e.getMessage());
            return null;
        }
    }

    private void writeCache(List<ProjectView> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(CACHE_KEY, json, CACHE_TTL);
        } catch (Exception e) {
            log.warn("写入 OpenDigger 缓存失败: {}", e.getMessage());
        }
    }

    private Map<String, Double> fetchMetricWithCache(RepoConfig config, String metric) throws IOException, InterruptedException {
        String cacheKey = METRIC_CACHE_KEY.formatted(config.repoPath(), metric);
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                return objectMapper.readValue(cached, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.warn("读取指标缓存失败 {} {}: {}", config.repoPath(), metric, e.getMessage());
        }

        String url = String.format("%s/%s/%s/%s.json", BASE_URL, config.owner(), config.repo(), metric);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "openrank-demo")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            log.warn("OpenDigger 请求失败 {} {} HTTP {}", config.repoPath(), metric, response.statusCode());
            return Map.of();
        }

        Map<String, Double> result = objectMapper.readValue(response.body(), new TypeReference<>() {});
        try {
            redisTemplate.opsForValue().set(cacheKey, response.body(), CACHE_TTL);
        } catch (Exception e) {
            log.warn("写入指标缓存失败 {} {}: {}", config.repoPath(), metric, e.getMessage());
        }
        return result;
    }

    private Optional<YearMonth> latestYearMonth(Map<String, Double> metric) {
        return metric.keySet().stream()
                .filter(key -> key.matches("\\d{4}-\\d{2}"))
                .map(YearMonth::parse)
                .max(Comparator.naturalOrder());
    }

    private double metricValue(Map<String, Double> metric, YearMonth month) {
        if (month == null) {
            return 0.0;
        }
        return metric.getOrDefault(month.format(YEAR_MONTH), 0.0);
    }

    private double roundToSingleDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private List<String> lastMonths(YearMonth latest, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> latest.minusMonths(count - 1 - i).format(YEAR_MONTH))
                .toList();
    }

    private record RepoConfig(
            String owner,
            String repo,
            String displayName,
            String maintainer,
            String description,
            List<String> tags,
            String status,
            String priority,
            List<String> tasks
    ) {
        String repoPath() {
            return owner + "/" + repo;
        }
    }
}
