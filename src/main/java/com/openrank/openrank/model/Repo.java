package com.openrank.openrank.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Repo {
    private Long id;
    private String owner;
    private String repo;
    private String fullName;
    private String displayName;
    private String description;
    private String tags;
    private String status;
    private String priority;
    private Double openrank;
    private Long stars;
    private String period;
    private String periodType;
    private LocalDateTime updatedAt;
}
