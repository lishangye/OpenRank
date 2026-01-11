package com.openrank.openrank.controller;

import com.openrank.openrank.model.Favorite;
import com.openrank.openrank.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "收藏", description = "用户收藏仓库相关接口，需要 X-Token 头")
public class FavoriteController {

    private static final Logger log = LoggerFactory.getLogger(FavoriteController.class);
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Operation(
            summary = "获取收藏列表",
            description = "根据 X-Token 获取当前用户收藏的仓库列表。",
            parameters = @Parameter(name = "X-Token", description = "登录后颁发的 token", required = false),
            responses = @ApiResponse(responseCode = "200", description = "收藏列表", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Favorite.class))))
    )
    @GetMapping
    public ResponseEntity<List<Favorite>> list(@RequestHeader(value = "X-Token", required = false) String token) {
        List<Favorite> favorites = favoriteService.listByToken(token);
        return ResponseEntity.ok(favorites);
    }

    @Operation(
        summary = "添加收藏",
        description = "传入 repo（owner/repo），未登录或参数缺失返回 401。",
        parameters = @Parameter(name = "X-Token", description = "登录后颁发的 token", required = false),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "请求体：{ \"repo\": \"owner/repo\" }",
                required = true,
                content = @Content(schema = @Schema(implementation = Map.class))
        ),
        responses = {
                @ApiResponse(responseCode = "200", description = "已关注", content = @Content(schema = @Schema(implementation = Map.class))),
                @ApiResponse(responseCode = "401", description = "未登录或参数错误", content = @Content(schema = @Schema(implementation = Map.class)))
        }
    )
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

    @Operation(
        summary = "取消收藏",
        description = "传入 repo（owner/repo），未登录或参数缺失返回 401。",
        parameters = @Parameter(name = "X-Token", description = "登录后颁发的 token", required = false),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "请求体：{ \"repo\": \"owner/repo\" }",
                required = true,
                content = @Content(schema = @Schema(implementation = Map.class))
        ),
        responses = {
                @ApiResponse(responseCode = "200", description = "已取消关注", content = @Content(schema = @Schema(implementation = Map.class))),
                @ApiResponse(responseCode = "401", description = "未登录或参数错误", content = @Content(schema = @Schema(implementation = Map.class)))
        }
    )
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

    @Operation(
        summary = "收藏状态切换",
        description = "已收藏则取消，未收藏则添加。",
        parameters = @Parameter(name = "X-Token", description = "登录后颁发的 token", required = false),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "请求体：{ \"repo\": \"owner/repo\" }",
                required = true,
                content = @Content(schema = @Schema(implementation = Map.class))
        ),
        responses = {
                @ApiResponse(responseCode = "200", description = "切换成功", content = @Content(schema = @Schema(implementation = Map.class))),
                @ApiResponse(responseCode = "401", description = "未登录或参数错误", content = @Content(schema = @Schema(implementation = Map.class))),
                @ApiResponse(responseCode = "400", description = "repo 为空", content = @Content(schema = @Schema(implementation = Map.class)))
        }
    )
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
