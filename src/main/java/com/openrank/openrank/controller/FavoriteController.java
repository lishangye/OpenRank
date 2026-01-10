package com.openrank.openrank.controller;

import com.openrank.openrank.model.Favorite;
import com.openrank.openrank.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private static final Logger log = LoggerFactory.getLogger(FavoriteController.class);
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ResponseEntity<List<Favorite>> list(@RequestHeader(value = "X-Token", required = false) String token) {
        List<Favorite> favorites = favoriteService.listByToken(token);
        return ResponseEntity.ok(favorites);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> add(@RequestHeader(value = "X-Token", required = false) String token,
                                                   @RequestBody Map<String, String> body) {
        String repo = body.get("repo");
        boolean ok = favoriteService.addFavorite(token, repo);
        if (!ok) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录或参数错误"));
        }
        return ResponseEntity.ok(Map.of("message", "已关注"));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> remove(@RequestHeader(value = "X-Token", required = false) String token,
                                                      @RequestBody Map<String, String> body) {
        String repo = body.get("repo");
        boolean ok = favoriteService.removeFavorite(token, repo);
        if (!ok) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录或参数错误"));
        }
        return ResponseEntity.ok(Map.of("message", "已取消关注"));
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggle(@RequestHeader(value = "X-Token", required = false) String token,
                                                      @RequestBody Map<String, String> body) {
        String repo = body.get("repo");
        if (repo == null || repo.isBlank()) {
            log.warn("toggle favorite missing repo, token={}", shortToken(token));
            return ResponseEntity.badRequest().body(Map.of("message", "repo 不能为空"));
        }
        log.info("toggle favorite repo={}, token={}", repo, shortToken(token));
        Boolean state = favoriteService.toggle(token, repo);
        if (state == null) {
            log.warn("toggle favorite unauthorized or invalid token, repo={}, token={}", repo, shortToken(token));
            return ResponseEntity.status(401).body(Map.of("message", "未登录或参数错误"));
        }
        return ResponseEntity.ok(Map.of("favorited", state, "message", state ? "已关注" : "已取消关注"));
    }

    private String shortToken(String token) {
        if (token == null) return "null";
        return token.length() > 8 ? token.substring(0, 8) + "..." : token;
    }
}
