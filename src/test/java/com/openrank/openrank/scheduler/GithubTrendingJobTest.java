package com.openrank.openrank.scheduler;

import com.openrank.openrank.OpenrankApplication;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 手动触发 GitHub 热门仓库同步的简单验证。
 * 若未配置 GITHUB_TOKEN 则跳过测试。
 */
@SpringBootTest(classes = OpenrankApplication.class)
class GithubTrendingJobTest {

    @Autowired
    private GithubTrendingJob job;

    @Value("${github.token:}")
    private String token;

    @Test
    void triggerSyncManually() {
        // 未配置 token 时跳过，防止请求失败
        Assumptions.assumeTrue(token != null && !token.isBlank(), "GITHUB_TOKEN 未配置，跳过调用");
        job.triggerSync(1); // 只拉取 1 页，避免大量请求
    }
}
