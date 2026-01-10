package com.openrank.openrank.model;

import lombok.Data;

@Data
public class Favorite {
    private Long id;
    private Long userId;
    private String repo;
}
