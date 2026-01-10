package com.openrank.openrank.model;

import java.util.List;


public record ProjectView(
        String name,
        String repo,
        String maintainer,
        String updated,
        long stars,
        String trend,
        List<String> tags,
        String status,
        String priority,
        String description,
        List<String> tasks,
        double openrank
) {
}
