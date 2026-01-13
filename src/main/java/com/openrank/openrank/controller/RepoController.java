package com.openrank.openrank.controller;

import com.openrank.openrank.mapper.FavoriteMapper;
import com.openrank.openrank.mapper.RepoMapper;
import com.openrank.openrank.model.Favorite;
import com.openrank.openrank.model.Repo;
import com.openrank.openrank.service.AuthService;
import com.openrank.openrank.service.GithubRepoImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/repo")
@Tag(name = "仓库信息", description = "从 repo 表按 owner/repo 查询仓库详情")
public class RepoController {

    private final RepoMapper repoMapper;
    private final GithubRepoImportService githubRepoImportService;
    private final FavoriteMapper favoriteMapper;
    private final AuthService authService;

    public RepoController(RepoMapper repoMapper,
                          GithubRepoImportService githubRepoImportService,
                          FavoriteMapper favoriteMapper,
                          AuthService authService) {
        this.repoMapper = repoMapper;
        this.githubRepoImportService = githubRepoImportService;
        this.favoriteMapper = favoriteMapper;
        this.authService = authService;
    }

    @Operation(
            summary = "按 owner/repo 查询仓库",
            description = "路径参数 owner/repo，查不到返回 404。",
            responses = {
                    @ApiResponse(responseCode = "200", description = "仓库信息", content = @Content(schema = @Schema(implementation = Repo.class))),
                    @ApiResponse(responseCode = "404", description = "未找到仓库", content = @Content(schema = @Schema(implementation = Map.class)))
            }
    )
    @GetMapping("/{owner}/{repo}")
    public ResponseEntity<?> getByOwnerRepo(@PathVariable("owner") String owner,
                                            @PathVariable("repo") String repo) {
        Repo record = repoMapper.findByOwnerAndRepo(owner, repo);
        if (record == null) {
            return ResponseEntity.status(404).body(Map.of("message", "未找到仓库"));
        }
        return ResponseEntity.ok(record);
    }

    @Operation(
        summary = "按 full_name 查询仓库",
        description = "路径参数 fullName=owner/repo，查不到返回 404。",
        responses = {
                @ApiResponse(responseCode = "200", description = "仓库信息", content = @Content(schema = @Schema(implementation = Repo.class))),
                @ApiResponse(responseCode = "404", description = "未找到仓库", content = @Content(schema = @Schema(implementation = Map.class)))
        }
    )
    @GetMapping("/full/{fullName:.+}")
    public ResponseEntity<?> getByFullName(@PathVariable("fullName") String fullName) {
        Repo record = repoMapper.findByFullName(fullName);
        if (record == null) {
            return ResponseEntity.status(404).body(Map.of("message", "未找到仓库"));
        }
        return ResponseEntity.ok(record);
    }

    @Operation(
            summary = "按 full_name（query 参数）查询仓库",
            description = "通过请求参数 fullName=owner/repo 查询仓库，避免路径包含 / 导致 400。",
            responses = {
                    @ApiResponse(responseCode = "200", description = "仓库信息", content = @Content(schema = @Schema(implementation = Repo.class))),
                    @ApiResponse(responseCode = "404", description = "未找到仓库", content = @Content(schema = @Schema(implementation = Map.class)))
            }
    )
    @GetMapping("/full")
    public ResponseEntity<?> getByFullNameParam(@RequestParam("fullName") String fullName) {
        Repo record = repoMapper.findByFullName(fullName);
        if (record == null) {
            return ResponseEntity.status(404).body(Map.of("message", "未找到仓库"));
        }
        return ResponseEntity.ok(record);
    }

    @Operation(
            summary = "导入并关注 GitHub 仓库",
            description = "输入 fullName(owner/repo)，从 GitHub 获取信息入库，若已登录则自动关注。",
            responses = {
                    @ApiResponse(responseCode = "200", description = "导入成功", content = @Content(schema = @Schema(implementation = Repo.class))),
                    @ApiResponse(responseCode = "400", description = "参数错误"),
                    @ApiResponse(responseCode = "500", description = "导入失败")
            }
    )
    @PostMapping("/import")
    public ResponseEntity<?> importRepo(@RequestParam("fullName") String fullName, HttpServletRequest request) {
        if (fullName == null || !fullName.contains("/")) {
            return ResponseEntity.badRequest().body(Map.of("message", "fullName 格式应为 owner/repo"));
        }
        try {
            Repo repo = githubRepoImportService.importByFullName(fullName);
            Long userId = resolveUserId(request);
          
            if (userId != null&&repo!=null) {
                Favorite f= new Favorite();
                f.setUserId(userId);
                f.setRepo(repo.getFullName());
                favoriteMapper.insertFavorite(f);
            }
            return ResponseEntity.ok(repo);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("message", "导入失败: " + ex.getMessage()));
        }
    }

    private Long resolveUserId(HttpServletRequest request) {
        // 1) 尝试从 Spring Security 上下文获取
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getPrincipal() instanceof String principal
                && !"anonymousUser".equalsIgnoreCase(principal)) {
            var user = authService.findByUsername(principal);
            if (user != null && user.getId() != null) return user.getId();
        }
        // 优先从 session 获取
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object uidAttr = session.getAttribute("userId");
            if (uidAttr instanceof Long) {
                return (Long) uidAttr;
            }
            if (uidAttr instanceof String) {
                try {
                    return Long.parseLong((String) uidAttr);
                } catch (NumberFormatException ignored) { }
            }
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            Long uid = authService.resolveUserId(authHeader.substring(7));
            if (uid != null) return uid;
        }
        String token = request.getHeader("X-Auth-Token");
        if (token != null && !token.isBlank()) {
            return authService.resolveUserId(token);
        }
        return null;
    }
}
