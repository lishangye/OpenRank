package com.openrank.openrank.controller;

import com.openrank.openrank.model.ProjectView;
import com.openrank.openrank.service.DiscoveryService;
import com.openrank.openrank.service.OpenDiggerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final OpenDiggerService openDiggerService;
    private final DiscoveryService discoveryService;

    public ProjectController(OpenDiggerService openDiggerService, DiscoveryService discoveryService) {
        this.openDiggerService = openDiggerService;
        this.discoveryService = discoveryService;
    }

    @GetMapping
    public List<ProjectView> list() {
        return openDiggerService.fetchProjects();
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics(@RequestParam("repo") String repo) {
        return openDiggerService.metricSeries(repo);
    }

    @GetMapping("/discovery")
    public List<ProjectView> discovery(@RequestParam(value = "limit", defaultValue = "12") int limit,
                                       @RequestParam(value = "periodType", defaultValue = "MONTH") String periodType,
                                       @RequestParam(value = "order", defaultValue = "openrank") String order) {
        limit = Math.min(Math.max(limit, 1), 50);
        // 只从数据库表 repo_ranking 获取排行数据（不再回退外部接口）
        return discoveryService.fromDbPublic(periodType.toUpperCase(), order, limit);
    }

    /**
     * 汇总所有可用仓库（数据库榜单 + 预置列表），用于“全部仓库”展示。
     */
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
