package com.openrank.openrank.service;

import com.openrank.openrank.mapper.FavoriteMapper;
import com.openrank.openrank.model.Favorite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class FavoriteService {

    private static final Logger log = LoggerFactory.getLogger(FavoriteService.class);
    private final FavoriteMapper favoriteMapper;
    private final AuthService authService;

    public FavoriteService(FavoriteMapper favoriteMapper, AuthService authService) {
        this.favoriteMapper = favoriteMapper;
        this.authService = authService;
    }

    public List<Favorite> listByToken(String token) {
        Long userId = authService.resolveUserId(token);
        if (userId == null) {
            return Collections.emptyList();
        }
        return favoriteMapper.findByUserId(userId);
    }

    public boolean addFavorite(String token, String repo) {
        Long userId = authService.resolveUserId(token);
        if (userId == null || !StringUtils.hasText(repo)) {
            return false;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setRepo(repo);
        favoriteMapper.insertFavorite(favorite);
        log.info("add favorite userId={}, repo={}", userId, repo);
        return true;
    }

    public boolean removeFavorite(String token, String repo) {
        Long userId = authService.resolveUserId(token);
        if (userId == null || !StringUtils.hasText(repo)) {
            return false;
        }
        favoriteMapper.deleteFavorite(userId, repo);
        log.info("remove favorite userId={}, repo={}", userId, repo);
        return true;
    }

    public Boolean toggle(String token, String repo) {
        Long userId = authService.resolveUserId(token);
        if (userId == null || !StringUtils.hasText(repo)) {
            return null;
        }
        Favorite exists = favoriteMapper.findOne(userId, repo);
        if (exists != null) {
            favoriteMapper.deleteFavorite(userId, repo);
            log.info("toggle favorite remove userId={}, repo={}", userId, repo);
            return false;
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setRepo(repo);
            favoriteMapper.insertFavorite(favorite);
            log.info("toggle favorite add userId={}, repo={}", userId, repo);
            return true;
        }
    }
}
