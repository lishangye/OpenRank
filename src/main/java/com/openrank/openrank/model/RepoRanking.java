package com.openrank.openrank.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RepoRanking {
    private Long id;
    private String owner;
    private String repo;
    private String displayName;
    private String periodType; // WEEK / MONTH
    private String period;     // 2026-01 or 2026-W02
    private Integer rank;
    private Double openrank;
    private Long stars;
    private Long deltaStars;
    private Double attention;
    private Double activity;
    private String description;
    private String tags;       // 逗号分隔
    private LocalDateTime updatedAt;
}
