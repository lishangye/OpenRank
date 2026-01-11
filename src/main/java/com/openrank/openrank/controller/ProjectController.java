package com.openrank.openrank.controller;

import com.openrank.openrank.model.ProjectView;
import com.openrank.openrank.service.DiscoveryService;
import com.openrank.openrank.service.OpenDiggerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "项目", description = "榜单/项目数据接口")
public class ProjectController {

    private final OpenDiggerService openDiggerService;
    private final DiscoveryService discoveryService;

    public ProjectController(OpenDiggerService openDiggerService, DiscoveryService discoveryService) {
        this.openDiggerService = openDiggerService;
        this.discoveryService = discoveryService;
    }

    @Operation(
            summary = "获取预置/缓存项目清单",
            description = "返回 OpenDigger 预置列表或缓存项目列表。",
            responses = @ApiResponse(responseCode = "200", description = "项目列表", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjectView.class))))
    )
    @GetMapping
    public List<ProjectView> list() {
        return openDiggerService.fetchProjects();
    }

    @Operation(
            summary = "获取仓库指标序列",
            description = "返回指定 repo 的 OpenRank/Stars 序列（最长 12 期）。",
            parameters = @Parameter(name = "repo", description = "仓库路径 owner/repo", required = true),
            responses = @ApiResponse(responseCode = "200", description = "指标序列", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @GetMapping("/metrics")
    public Map<String, Object> metrics(@RequestParam("repo") String repo) {
        return openDiggerService.metricSeries(repo);
    }

    @Operation(
            summary = "获取排行榜",
            description = "从数据库 repo_ranking 获取最新一期榜单。",
            parameters = {
                    @Parameter(name = "limit", description = "返回数量上限，默认12，最大50"),
                    @Parameter(name = "periodType", description = "周期类型：MONTH/WEEK，默认 MONTH"),
                    @Parameter(name = "order", description = "排序字段：openrank/stars/delta_stars/attention/activity/rank，默认 openrank")
            },
            responses = @ApiResponse(responseCode = "200", description = "榜单列表", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjectView.class))))
    )
    @GetMapping("/discovery")
    public List<ProjectView> discovery(@RequestParam(value = "limit", defaultValue = "12") int limit,
                                       @RequestParam(value = "periodType", defaultValue = "MONTH") String periodType,
                                       @RequestParam(value = "order", defaultValue = "openrank") String order) {
        limit = Math.min(Math.max(limit, 1), 50);
        // 只从数据库表 repo_ranking 获取排行数据（不再回退外部接口）
        return discoveryService.fromDbPublic(periodType.toUpperCase(), order, limit);
    }

    @Operation(
            summary = "获取全部仓库",
            description = "汇总数据库榜单（周/月）与预置列表，用于“全部仓库”展示。",
            parameters = @Parameter(name = "limit", description = "数量上限，默认500，最大500"),
            responses = @ApiResponse(responseCode = "200", description = "全部仓库列表", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjectView.class))))
    )
    @GetMapping("/all")
    public List<ProjectView> all(@RequestParam(value = "limit", defaultValue = "500") int limit) {
        limit = Math.min(Math.max(limit, 1), 500);
        java.util.LinkedHashMap<String, ProjectView> map = new java.util.LinkedHashMap<>();
        try {
            discoveryService.fromDbPublic("MONTH", "rank", limit).forEach(p -> map.put(p.repo(), p));
        } catch (Exception ignored) { }
        try {
            discoveryService.fromDbPublic("WEEK", "rank", limit).forEach(p -> map.putIfAbsent(p.repo(), p));
        } catch (Exception ignored) { }
        try {
            openDiggerService.fetchProjects().forEach(p -> map.putIfAbsent(p.repo(), p));
        } catch (Exception ignored) { }
        // 如果仍为空，至少返回预置列表避免前端为空
        if (map.isEmpty()) {
            return openDiggerService.fetchProjects();
        }
        return new java.util.ArrayList<>(map.values());
    }
}
