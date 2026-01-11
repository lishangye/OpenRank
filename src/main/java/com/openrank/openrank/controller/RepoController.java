package com.openrank.openrank.controller;

import com.openrank.openrank.mapper.RepoMapper;
import com.openrank.openrank.model.Repo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/repo")
@Tag(name = "仓库信息", description = "从 repo 表按 owner/repo 查询仓库详情")
public class RepoController {

    private final RepoMapper repoMapper;

    public RepoController(RepoMapper repoMapper) {
        this.repoMapper = repoMapper;
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

    /**
     * 通过请求参数 fullName 查询，避免路径中包含 / 导致 400。
     * 调用示例：/api/repo/full?fullName=owner/repo （建议对 fullName 进行 URL 编码）
     */
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
}
