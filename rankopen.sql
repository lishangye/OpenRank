/*
 Navicat Premium Data Transfer

 Source Server         : 本机
 Source Server Type    : MySQL
 Source Server Version : 80034
 Source Host           : localhost:3306
 Source Schema         : rankopen

 Target Server Type    : MySQL
 Target Server Version : 80034
 File Encoding         : 65001

 Date: 13/01/2026 22:36:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `repo` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'owner/repo',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_user_repo`(`user_id`, `repo`) USING BTREE,
  INDEX `idx_repo`(`repo`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户关注的仓库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (4, 1, 'vercel/next.js', '2026-01-10 16:52:56');
INSERT INTO `favorite` VALUES (13, 1, 'huggingface/transformers', '2026-01-10 22:58:28');
INSERT INTO `favorite` VALUES (15, 2, 'apache/kafka', '2026-01-11 23:15:05');
INSERT INTO `favorite` VALUES (16, 2, 'apache/iceberg', '2026-01-11 23:15:06');
INSERT INTO `favorite` VALUES (17, 2, 'DigitalPlatDev/FreeDomain', '2026-01-11 23:15:09');
INSERT INTO `favorite` VALUES (18, 2, 'apache/spark', '2026-01-11 23:20:43');
INSERT INTO `favorite` VALUES (20, 1, 'microsoft/vscode', '2026-01-13 01:55:11');
INSERT INTO `favorite` VALUES (22, 1, 'apache/iceberg', '2026-01-13 01:55:29');
INSERT INTO `favorite` VALUES (26, 1, 'lishangye/OpenRank', '2026-01-13 01:58:05');
INSERT INTO `favorite` VALUES (27, 1, 'facebook/react-native', '2026-01-13 02:06:03');
INSERT INTO `favorite` VALUES (28, 3, 'apache/kafka', '2026-01-13 02:06:45');
INSERT INTO `favorite` VALUES (29, 3, 'apache/iceberg', '2026-01-13 02:06:50');

-- ----------------------------
-- Table structure for repo
-- ----------------------------
DROP TABLE IF EXISTS `repo`;
CREATE TABLE `repo`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `owner` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库所有者/组织',
  `repo` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库名',
  `full_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'owner/repo 唯一标识',
  `display_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '展示名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签，逗号分隔',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'production' COMMENT '状态：production/beta/lab',
  `priority` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'P2' COMMENT '优先级',
  `openrank` double NULL DEFAULT NULL COMMENT '最新 OpenRank 值',
  `stars` bigint NULL DEFAULT NULL COMMENT '最新 Star 总数',
  `period` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最新指标周期，如 2026-02',
  `period_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'MONTH' COMMENT '周期类型：MONTH/WEEK',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_full`(`full_name`) USING BTREE,
  INDEX `idx_owner_repo`(`owner`, `repo`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1070 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库主数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repo
-- ----------------------------
INSERT INTO `repo` VALUES (1, 'microsoft', 'vscode', 'microsoft/vscode', 'microsoft/vscode', 'Visual Studio Code', 'github,auto', 'production', 'P2', 779.68, 180546, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (2, 'pytorch', 'pytorch', 'pytorch/pytorch', 'pytorch/pytorch', 'Tensors and Dynamic neural networks in Python with strong GPU acceleration', 'github,auto', 'production', 'P2', 972.9, 96525, '2026-01-01', 'MONTH', '2026-01-12 00:00:12');
INSERT INTO `repo` VALUES (3, 'vercel', 'next.js', 'vercel/next.js', 'vercel/next.js', 'The React Framework', 'github,auto', 'production', 'P2', 390.21, 137082, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (7, 'apache', 'spark', 'apache/spark', 'Apache Spark', '大数据计算引擎', 'data,infra', 'production', 'P2', 5300.3, 36000, '2026-W02', 'MONTH', '2026-01-11 18:44:11');
INSERT INTO `repo` VALUES (8, 'apache', 'flink', 'apache/flink', 'Apache Flink', '流批一体引擎', 'streaming,infra', 'production', 'P2', 4800.7, 27000, '2026-W02', 'MONTH', '2026-01-11 18:44:14');
INSERT INTO `repo` VALUES (9, 'huggingface', 'transformers', 'huggingface/transformers', 'huggingface/transformers', '🤗 Transformers: the model-definition framework for state-of-the-art machine learning models in text, vision, audio, and multimodal models, for both inference and training. ', 'github,manual', 'production', 'P2', 389.16, 154958, '2026-01-01', 'MONTH', '2026-01-13 01:56:11');
INSERT INTO `repo` VALUES (10, 'openai', 'whisper', 'openai/whisper', 'Whisper', '语音识别模型', 'audio,ai', 'production', 'P2', 7600.4, 65000, '2026-02', 'MONTH', '2026-01-10 03:17:20');
INSERT INTO `repo` VALUES (11, 'apache', 'kafka', 'apache/kafka', 'Apache Kafka', '分布式消息流平台', 'streaming,infra', 'production', 'P2', 5100.5, 25000, '2026-W03', 'MONTH', '2026-01-11 18:44:17');
INSERT INTO `repo` VALUES (12, 'apache', 'iceberg', 'apache/iceberg', 'Apache Iceberg', '表格式湖存储', 'data,lakehouse', 'production', 'P2', 4700.9, 19000, '2026-W03', 'MONTH', '2026-01-11 18:44:20');
INSERT INTO `repo` VALUES (13, 'codecrafters-io', 'build-your-own-x', 'codecrafters-io/build-your-own-x', 'codecrafters-io/build-your-own-x', 'Master programming by recreating your favorite technologies from scratch.', 'github,auto', 'production', 'P2', 4.65, 455742, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (14, 'freeCodeCamp', 'freeCodeCamp', 'freeCodeCamp/freeCodeCamp', 'freeCodeCamp/freeCodeCamp', 'freeCodeCamp.org\'s open-source codebase and curriculum. Learn math, programming, and computer science for free.', 'github,auto', 'production', 'P2', 150.58, 435735, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (15, 'sindresorhus', 'awesome', 'sindresorhus/awesome', 'sindresorhus/awesome', '😎 Awesome lists about all kinds of interesting topics', 'github,auto', 'production', 'P2', 10.37, 428734, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (16, 'public-apis', 'public-apis', 'public-apis/public-apis', 'public-apis/public-apis', 'A collective list of free APIs', 'github,auto', 'production', 'P2', 11.27, 390582, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (17, 'EbookFoundation', 'free-programming-books', 'EbookFoundation/free-programming-books', 'EbookFoundation/free-programming-books', ':books: Freely available programming books', 'github,auto', 'production', 'P2', 57.36, 380278, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (18, 'kamranahmedse', 'developer-roadmap', 'kamranahmedse/developer-roadmap', 'kamranahmedse/developer-roadmap', 'Interactive roadmaps, guides and other educational content to help developers grow in their careers.', 'github,auto', 'production', 'P2', 108.14, 347050, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (19, 'jwasham', 'coding-interview-university', 'jwasham/coding-interview-university', 'jwasham/coding-interview-university', 'A complete computer science study plan to become a software engineer.', 'github,auto', 'production', 'P2', 3.61, 335786, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (20, 'donnemartin', 'system-design-primer', 'donnemartin/system-design-primer', 'donnemartin/system-design-primer', 'Learn how to design large-scale systems. Prep for the system design interview.  Includes Anki flashcards.', 'github,auto', 'production', 'P2', 6.6, 331921, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (21, 'vinta', 'awesome-python', 'vinta/awesome-python', 'vinta/awesome-python', 'An opinionated list of awesome Python frameworks, libraries, software and resources.', 'github,auto', 'production', 'P2', 3.27, 277750, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (22, '996icu', '996.ICU', '996icu/996.ICU', '996icu/996.ICU', 'Repo for counting stars and contributing. Press F to pay respect to glorious developers.', 'github,auto', 'production', 'P2', 0.99, 275140, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (23, 'awesome-selfhosted', 'awesome-selfhosted', 'awesome-selfhosted/awesome-selfhosted', 'awesome-selfhosted/awesome-selfhosted', 'A list of Free Software network services and web applications which can be hosted on your own servers', 'github,auto', 'production', 'P2', 0.11, 267617, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (24, 'practical-tutorials', 'project-based-learning', 'practical-tutorials/project-based-learning', 'practical-tutorials/project-based-learning', 'Curated list of project-based tutorials', 'github,auto', 'production', 'P2', 3.72, 255364, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (25, 'facebook', 'react', 'facebook/react', 'facebook/react', 'The library for web and native user interfaces.', 'github,auto', 'production', 'P2', 128.43, 242170, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (26, 'TheAlgorithms', 'Python', 'TheAlgorithms/Python', 'TheAlgorithms/Python', 'All Algorithms implemented in Python', 'github,auto', 'production', 'P2', 84.47, 216748, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (27, 'torvalds', 'linux', 'torvalds/linux', 'torvalds/linux', 'Linux kernel source tree', 'github,auto', 'production', 'P2', 71.45, 213216, '2026-01-01', 'MONTH', '2026-01-12 00:00:03');
INSERT INTO `repo` VALUES (28, 'vuejs', 'vue', 'vuejs/vue', 'vuejs/vue', 'This is the repo for Vue 2. For Vue 3, go to https://github.com/vuejs/core', 'github,auto', 'production', 'P2', 3.58, 209832, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (29, 'trimstray', 'the-book-of-secret-knowledge', 'trimstray/the-book-of-secret-knowledge', 'trimstray/the-book-of-secret-knowledge', 'A collection of inspiring lists, manuals, cheatsheets, blogs, hacks, one-liners, cli/web tools and more.', 'github,auto', 'production', 'P2', 1.51, 201887, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (30, 'ossu', 'computer-science', 'ossu/computer-science', 'ossu/computer-science', '🎓 Path to a free self-taught education in Computer Science!', 'github,auto', 'production', 'P2', 3.67, 200097, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (31, 'trekhleb', 'javascript-algorithms', 'trekhleb/javascript-algorithms', 'trekhleb/javascript-algorithms', '📝 Algorithms and data structures implemented in JavaScript with explanations and links to further readings', 'github,auto', 'production', 'P2', 1.42, 195308, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (32, 'tensorflow', 'tensorflow', 'tensorflow/tensorflow', 'tensorflow/tensorflow', 'An Open Source Machine Learning Framework for Everyone', 'github,auto', 'production', 'P2', 98.11, 193293, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (33, 'getify', 'You-Dont-Know-JS', 'getify/You-Dont-Know-JS', 'getify/You-Dont-Know-JS', 'A book series (2 published editions) on the JS language.', 'github,auto', 'production', 'P2', 1.51, 184253, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (34, 'ohmyzsh', 'ohmyzsh', 'ohmyzsh/ohmyzsh', 'ohmyzsh/ohmyzsh', '🙃   A delightful community-driven (with 2,400+ contributors) framework for managing your zsh configuration. Includes 300+ optional plugins (rails, git, macOS, hub, docker, homebrew, node, php, python, etc), 140+ themes to spice up your morning, and an auto-update tool that makes it easy to keep up with the latest updates from the community.', 'github,auto', 'production', 'P2', 26.67, 183882, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (35, 'CyC2018', 'CS-Notes', 'CyC2018/CS-Notes', 'CyC2018/CS-Notes', ':books: 技术面试必备基础知识、Leetcode、计算机操作系统、计算机网络、系统设计', 'github,auto', 'production', 'P2', 3, 183389, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (36, 'Significant-Gravitas', 'AutoGPT', 'Significant-Gravitas/AutoGPT', 'Significant-Gravitas/AutoGPT', 'AutoGPT is the vision of accessible AI for everyone, to use and to build on. Our mission is to provide the tools, so that you can focus on what matters.', 'github,auto', 'production', 'P2', 44, 181006, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (38, 'jackfrued', 'Python-100-Days', 'jackfrued/Python-100-Days', 'jackfrued/Python-100-Days', 'Python - 100天从新手到大师', 'github,auto', 'production', 'P2', 1.73, 177350, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (39, 'flutter', 'flutter', 'flutter/flutter', 'flutter/flutter', 'Flutter makes it easy and fast to build beautiful apps for mobile and beyond', 'github,auto', 'production', 'P2', 562.1, 174647, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (40, 'twbs', 'bootstrap', 'twbs/bootstrap', 'twbs/bootstrap', 'The most popular HTML, CSS, and JavaScript framework for developing responsive, mobile first projects on the web.', 'github,auto', 'production', 'P2', 21.6, 173903, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (41, 'github', 'gitignore', 'github/gitignore', 'github/gitignore', 'A collection of useful .gitignore templates', 'github,auto', 'production', 'P2', 6.64, 171725, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (42, 'n8n-io', 'n8n', 'n8n-io/n8n', 'n8n-io/n8n', 'Fair-code workflow automation platform with native AI capabilities. Combine visual building with custom code, self-host or cloud, 400+ integrations.', 'github,auto', 'production', 'P2', 124.79, 168114, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (43, 'avelino', 'awesome-go', 'avelino/awesome-go', 'avelino/awesome-go', 'A curated list of awesome Go frameworks, libraries and software', 'github,auto', 'production', 'P2', 9.84, 162280, '2026-01-01', 'MONTH', '2026-01-12 00:00:04');
INSERT INTO `repo` VALUES (44, 'massgravel', 'Microsoft-Activation-Scripts', 'massgravel/Microsoft-Activation-Scripts', 'massgravel/Microsoft-Activation-Scripts', 'Open-source Windows and Office activator featuring HWID, Ohook, TSforge, and Online KMS activation methods, along with advanced troubleshooting.', 'github,auto', 'production', 'P2', 14.58, 162195, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (45, 'AUTOMATIC1111', 'stable-diffusion-webui', 'AUTOMATIC1111/stable-diffusion-webui', 'AUTOMATIC1111/stable-diffusion-webui', 'Stable Diffusion web UI', 'github,auto', 'production', 'P2', 33.2, 159843, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (46, 'jlevy', 'the-art-of-command-line', 'jlevy/the-art-of-command-line', 'jlevy/the-art-of-command-line', 'Master the command line, in one page', 'github,auto', 'production', 'P2', 0.88, 159427, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (47, 'ollama', 'ollama', 'ollama/ollama', 'ollama/ollama', 'Get up and running with OpenAI gpt-oss, DeepSeek-R1, Gemma 3 and other models.', 'github,auto', 'production', 'P2', 285.2, 159195, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (49, 'Snailclimb', 'JavaGuide', 'Snailclimb/JavaGuide', 'Snailclimb/JavaGuide', 'Java 学习&面试指南（Go、Python 后端面试通用,计算机基础面试总结）。准备后端技术面试，首选 JavaGuide！', 'github,auto', 'production', 'P2', 12.11, 153409, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (50, 'airbnb', 'javascript', 'airbnb/javascript', 'airbnb/javascript', 'JavaScript Style Guide', 'github,auto', 'production', 'P2', 4.13, 148020, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (51, 'langflow-ai', 'langflow', 'langflow-ai/langflow', 'langflow-ai/langflow', 'Langflow is a powerful tool for building and deploying AI-powered agents and workflows.', 'github,auto', 'production', 'P2', 122.69, 143255, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (52, 'f', 'awesome-chatgpt-prompts', 'f/awesome-chatgpt-prompts', 'f/awesome-chatgpt-prompts', 'Share, discover, and collect prompts from the community. Free and open source — self-host for your organization with complete privacy.', 'github,auto', 'production', 'P2', 3.21, 142079, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (53, 'yt-dlp', 'yt-dlp', 'yt-dlp/yt-dlp', 'yt-dlp/yt-dlp', 'A feature-rich command-line audio/video downloader', 'github,auto', 'production', 'P2', 146.77, 141391, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (54, '521xueweihan', 'HelloGitHub', '521xueweihan/HelloGitHub', '521xueweihan/HelloGitHub', ':octocat: 分享 GitHub 上有趣、入门级的开源项目。Share interesting, entry-level open source projects on GitHub.', 'github,auto', 'production', 'P2', 3.22, 139760, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (55, 'ytdl-org', 'youtube-dl', 'ytdl-org/youtube-dl', 'ytdl-org/youtube-dl', 'Command-line program to download videos from YouTube.com and other video sites', 'github,auto', 'production', 'P2', 15.37, 139413, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (56, 'DigitalPlatDev', 'FreeDomain', 'DigitalPlatDev/FreeDomain', 'DigitalPlatDev/FreeDomain', 'DigitalPlat FreeDomain: Free Domain For Everyone', 'github,auto', 'production', 'P2', 945.75, 139509, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (58, 'yangshun', 'tech-interview-handbook', 'yangshun/tech-interview-handbook', 'yangshun/tech-interview-handbook', 'Curated coding interview preparation materials for busy software engineers', 'github,auto', 'production', 'P2', 2.81, 136688, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (59, 'Genymobile', 'scrcpy', 'Genymobile/scrcpy', 'Genymobile/scrcpy', 'Display and control your Android device', 'github,auto', 'production', 'P2', 49.7, 133911, '2026-01-01', 'MONTH', '2026-01-12 00:00:05');
INSERT INTO `repo` VALUES (60, 'labuladong', 'fucking-algorithm', 'labuladong/fucking-algorithm', 'labuladong/fucking-algorithm', '刷算法全靠套路，认准 labuladong 就够了！English version supported! Crack LeetCode, not only how, but also why. ', 'github,auto', 'production', 'P2', 4.6, 132044, '2026-01-01', 'MONTH', '2026-01-12 00:00:06');
INSERT INTO `repo` VALUES (61, 'golang', 'go', 'golang/go', 'golang/go', 'The Go programming language', 'github,auto', 'production', 'P2', 190.79, 131893, '2026-01-01', 'MONTH', '2026-01-12 00:00:06');
INSERT INTO `repo` VALUES (62, 'microsoft', 'PowerToys', 'microsoft/PowerToys', 'microsoft/PowerToys', 'Microsoft PowerToys is a collection of utilities that help you customize Windows and streamline everyday tasks', 'github,auto', 'production', 'P2', 206.5, 127816, '2026-01-01', 'MONTH', '2026-01-12 00:00:06');
INSERT INTO `repo` VALUES (63, 'Chalarangelo', '30-seconds-of-code', 'Chalarangelo/30-seconds-of-code', 'Chalarangelo/30-seconds-of-code', 'Coding articles to level up your development skills', 'github,auto', 'production', 'P2', 2.18, 126274, '2026-01-01', 'MONTH', '2026-01-12 00:00:08');
INSERT INTO `repo` VALUES (64, 'langgenius', 'dify', 'langgenius/dify', 'langgenius/dify', 'Production-ready platform for agentic workflow development.', 'github,auto', 'production', 'P2', 377.59, 125521, '2026-01-01', 'MONTH', '2026-01-12 00:00:08');
INSERT INTO `repo` VALUES (65, 'facebook', 'react-native', 'facebook/react-native', 'facebook/react-native', 'A framework for building native applications using React', 'github,auto', 'production', 'P2', 189.03, 125030, '2026-01-01', 'MONTH', '2026-01-12 00:00:08');
INSERT INTO `repo` VALUES (66, 'langchain-ai', 'langchain', 'langchain-ai/langchain', 'langchain-ai/langchain', '🦜🔗 The platform for reliable agents.', 'github,auto', 'production', 'P2', 266.89, 123937, '2026-01-01', 'MONTH', '2026-01-12 00:00:08');
INSERT INTO `repo` VALUES (67, 'krahets', 'hello-algo', 'krahets/hello-algo', 'krahets/hello-algo', '《Hello 算法》：动画图解、一键运行的数据结构与算法教程。支持简中、繁中、English、日本語，提供 Python, Java, C++, C, C#, JS, Go, Swift, Rust, Ruby, Kotlin, TS, Dart 等代码实现', 'github,auto', 'production', 'P2', 12.54, 121419, '2026-01-01', 'MONTH', '2026-01-12 00:00:08');
INSERT INTO `repo` VALUES (68, 'open-webui', 'open-webui', 'open-webui/open-webui', 'open-webui/open-webui', 'User-friendly AI Interface (Supports Ollama, OpenAI API, ...)', 'github,auto', 'production', 'P2', 171.39, 120250, '2026-01-01', 'MONTH', '2026-01-12 00:00:08');
INSERT INTO `repo` VALUES (69, 'kubernetes', 'kubernetes', 'kubernetes/kubernetes', 'kubernetes/kubernetes', 'Production-Grade Container Scheduling and Management', 'github,auto', 'production', 'P2', 382.13, 119758, '2026-01-01', 'MONTH', '2026-01-12 00:00:08');
INSERT INTO `repo` VALUES (70, 'electron', 'electron', 'electron/electron', 'electron/electron', ':electron: Build cross-platform desktop apps with JavaScript, HTML, and CSS', 'github,auto', 'production', 'P2', 120.87, 119701, '2026-01-01', 'MONTH', '2026-01-12 00:00:08');
INSERT INTO `repo` VALUES (71, 'ripienaar', 'free-for-dev', 'ripienaar/free-for-dev', 'ripienaar/free-for-dev', 'A list of SaaS, PaaS and IaaS offerings that have free tiers of interest to devops and infradev', 'github,auto', 'production', 'P2', 12.96, 117207, '2026-01-01', 'MONTH', '2026-01-12 00:00:08');
INSERT INTO `repo` VALUES (72, 'justjavac', 'free-programming-books-zh_CN', 'justjavac/free-programming-books-zh_CN', 'justjavac/free-programming-books-zh_CN', ':books: 免费的计算机编程类中文书籍，欢迎投稿', 'github,auto', 'production', 'P2', 0.76, 116132, '2026-01-01', 'MONTH', '2026-01-12 00:00:09');
INSERT INTO `repo` VALUES (73, 'nodejs', 'node', 'nodejs/node', 'nodejs/node', 'Node.js JavaScript runtime ✨🐢🚀✨', 'github,auto', 'production', 'P2', 178.57, 115152, '2026-01-01', 'MONTH', '2026-01-12 00:00:09');
INSERT INTO `repo` VALUES (74, 'excalidraw', 'excalidraw', 'excalidraw/excalidraw', 'excalidraw/excalidraw', 'Virtual whiteboard for sketching hand-drawn like diagrams', 'github,auto', 'production', 'P2', 68.51, 114164, '2026-01-01', 'MONTH', '2026-01-12 00:00:09');
INSERT INTO `repo` VALUES (75, 'd3', 'd3', 'd3/d3', 'd3/d3', 'Bring data to life with SVG, Canvas and HTML. :bar_chart::chart_with_upwards_trend::tada:', 'github,auto', 'production', 'P2', 3.51, 112128, '2026-01-01', 'MONTH', '2026-01-12 00:00:09');
INSERT INTO `repo` VALUES (76, 'mrdoob', 'three.js', 'mrdoob/three.js', 'mrdoob/three.js', 'JavaScript 3D Library.', 'github,auto', 'production', 'P2', 83.4, 110248, '2026-01-01', 'MONTH', '2026-01-12 00:00:09');
INSERT INTO `repo` VALUES (77, 'rust-lang', 'rust', 'rust-lang/rust', 'rust-lang/rust', 'Empowering everyone to build reliable and efficient software.', 'github,auto', 'production', 'P2', 479.93, 109270, '2026-01-01', 'MONTH', '2026-01-12 00:00:10');
INSERT INTO `repo` VALUES (78, 'iptv-org', 'iptv', 'iptv-org/iptv', 'iptv-org/iptv', 'Collection of publicly available IPTV channels from all over the world', 'github,auto', 'production', 'P2', 57.82, 109097, '2026-01-01', 'MONTH', '2026-01-12 00:00:10');
INSERT INTO `repo` VALUES (79, 'axios', 'axios', 'axios/axios', 'axios/axios', 'Promise based HTTP client for the browser and node.js', 'github,auto', 'production', 'P2', 25.05, 108469, '2026-01-01', 'MONTH', '2026-01-12 00:00:10');
INSERT INTO `repo` VALUES (80, 'microsoft', 'TypeScript', 'microsoft/TypeScript', 'microsoft/TypeScript', 'TypeScript is a superset of JavaScript that compiles to clean JavaScript output.', 'github,auto', 'production', 'P2', 139.44, 107400, '2026-01-01', 'MONTH', '2026-01-12 00:00:10');
INSERT INTO `repo` VALUES (81, 'x1xhlol', 'system-prompts-and-models-of-ai-tools', 'x1xhlol/system-prompts-and-models-of-ai-tools', 'x1xhlol/system-prompts-and-models-of-ai-tools', 'FULL Augment Code, Claude Code, Cluely, CodeBuddy, Comet, Cursor, Devin AI, Junie, Kiro, Leap.new, Lovable, Manus, NotionAI, Orchids.app, Perplexity, Poke, Qoder, Replit, Same.dev, Trae, Traycer AI, VSCode Agent, Warp.dev, Windsurf, Xcode, Z.ai Code, Dia & v0. (And other Open Sourced) System Prompts, Internal Tools & AI Models', 'github,auto', 'production', 'P2', 0, 107358, '2026-01-01', 'MONTH', '2026-01-12 00:00:10');
INSERT INTO `repo` VALUES (82, 'denoland', 'deno', 'denoland/deno', 'denoland/deno', 'A modern runtime for JavaScript and TypeScript.', 'github,auto', 'production', 'P2', 187.92, 105783, '2026-01-01', 'MONTH', '2026-01-12 00:00:10');
INSERT INTO `repo` VALUES (83, 'rustdesk', 'rustdesk', 'rustdesk/rustdesk', 'rustdesk/rustdesk', 'An open-source remote desktop application designed for self-hosting, as an alternative to TeamViewer.', 'github,auto', 'production', 'P2', 64.55, 105509, '2026-01-01', 'MONTH', '2026-01-12 00:00:10');
INSERT INTO `repo` VALUES (84, 'godotengine', 'godot', 'godotengine/godot', 'godotengine/godot', 'Godot Engine – Multi-platform 2D and 3D game engine', 'github,auto', 'production', 'P2', 637.7, 105211, '2026-01-01', 'MONTH', '2026-01-12 00:00:10');
INSERT INTO `repo` VALUES (85, 'GrowingGit', 'GitHub-Chinese-Top-Charts', 'GrowingGit/GitHub-Chinese-Top-Charts', 'GrowingGit/GitHub-Chinese-Top-Charts', ':cn: GitHub中文排行榜，各语言分设「软件 | 资料」榜单，精准定位中文好项目。各取所需，高效学习。', 'github,auto', 'production', 'P2', 1.61, 105010, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (86, 'microsoft', 'generative-ai-for-beginners', 'microsoft/generative-ai-for-beginners', 'microsoft/generative-ai-for-beginners', '21 Lessons, Get Started Building with Generative AI ', 'github,auto', 'production', 'P2', 8.43, 104965, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (87, 'goldbergyoni', 'nodebestpractices', 'goldbergyoni/nodebestpractices', 'goldbergyoni/nodebestpractices', ':white_check_mark:  The Node.js best practices list (July 2024)', 'github,auto', 'production', 'P2', 0.67, 104917, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (88, 'shadcn-ui', 'ui', 'shadcn-ui/ui', 'shadcn-ui/ui', 'A set of beautifully-designed, accessible components and a code distribution platform. Works with your favorite frameworks. Open Source. Open Code.', 'github,auto', 'production', 'P2', 161.93, 104582, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (89, 'Hack-with-Github', 'Awesome-Hacking', 'Hack-with-Github/Awesome-Hacking', 'Hack-with-Github/Awesome-Hacking', 'A collection of various awesome lists for hackers, pentesters and security researchers', 'github,auto', 'production', 'P2', 1.13, 104274, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (90, 'facebook', 'create-react-app', 'facebook/create-react-app', 'facebook/create-react-app', 'Set up a modern web app by running one command.', 'github,auto', 'production', 'P2', 7.76, 103949, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (91, 'fatedier', 'frp', 'fatedier/frp', 'fatedier/frp', 'A fast reverse proxy to help you expose a local server behind a NAT or firewall to the internet.', 'github,auto', 'production', 'P2', 27.94, 103308, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (92, 'papers-we-love', 'papers-we-love', 'papers-we-love/papers-we-love', 'papers-we-love/papers-we-love', 'Papers from the computer science community to read and discuss.', 'github,auto', 'production', 'P2', 2.53, 102254, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (93, 'microsoft', 'terminal', 'microsoft/terminal', 'microsoft/terminal', 'The new Windows Terminal and the original Windows console host, all in the same place!', 'github,auto', 'production', 'P2', 75.11, 101334, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (94, 'tauri-apps', 'tauri', 'tauri-apps/tauri', 'tauri-apps/tauri', 'Build smaller, faster, and more secure desktop and mobile applications with a web frontend.', 'github,auto', 'production', 'P2', 109.79, 101076, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (95, 'deepseek-ai', 'DeepSeek-V3', 'deepseek-ai/DeepSeek-V3', 'deepseek-ai/DeepSeek-V3', NULL, 'github,auto', 'production', 'P2', 0, 101048, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (96, 'Comfy-Org', 'ComfyUI', 'Comfy-Org/ComfyUI', 'Comfy-Org/ComfyUI', 'The most powerful and modular diffusion model GUI, api and backend with a graph/nodes interface.', 'github,auto', 'production', 'P2', 0, 99805, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (97, 'angular', 'angular', 'angular/angular', 'angular/angular', 'Deliver web apps with confidence 🚀', 'github,auto', 'production', 'P2', 169.07, 99658, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (98, 'mui', 'material-ui', 'mui/material-ui', 'mui/material-ui', 'Material UI: Comprehensive React component library that implements Google\'s Material Design. Free forever.', 'github,auto', 'production', 'P2', 122.22, 97584, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (99, 'jaywcjlove', 'awesome-mac', 'jaywcjlove/awesome-mac', 'jaywcjlove/awesome-mac', ' Now we have become very big, Different from the original idea. Collect premium software in various categories.', 'github,auto', 'production', 'P2', 9.44, 97498, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (100, 'mtdvio', 'every-programmer-should-know', 'mtdvio/every-programmer-should-know', 'mtdvio/every-programmer-should-know', 'A collection of (mostly) technical things every software developer should know about', 'github,auto', 'production', 'P2', 1.8, 97455, '2026-01-01', 'MONTH', '2026-01-12 00:00:11');
INSERT INTO `repo` VALUES (101, 'ant-design', 'ant-design', 'ant-design/ant-design', 'ant-design/ant-design', 'An enterprise-class UI design language and React UI library', 'github,auto', 'production', 'P2', 139.57, 97218, '2026-01-01', 'MONTH', '2026-01-12 00:00:12');
INSERT INTO `repo` VALUES (102, 'Anduin2017', 'HowToCook', 'Anduin2017/HowToCook', 'Anduin2017/HowToCook', '程序员在家做饭方法指南。Programmer\'s guide about how to cook at home (Simplified Chinese only).', 'github,auto', 'production', 'P2', 2.43, 96993, '2026-01-01', 'MONTH', '2026-01-12 00:00:12');
INSERT INTO `repo` VALUES (104, 'supabase', 'supabase', 'supabase/supabase', 'supabase/supabase', 'The Postgres development platform. Supabase gives you a dedicated Postgres database to build your web, mobile, and AI applications.', 'github,auto', 'production', 'P2', 184.77, 95979, '2026-01-01', 'MONTH', '2026-01-12 00:00:12');
INSERT INTO `repo` VALUES (105, 'neovim', 'neovim', 'neovim/neovim', 'neovim/neovim', 'Vim-fork focused on extensibility and usability', 'github,auto', 'production', 'P2', 135.9, 95596, '2026-01-01', 'MONTH', '2026-01-12 00:00:12');
INSERT INTO `repo` VALUES (106, 'nvbn', 'thefuck', 'nvbn/thefuck', 'nvbn/thefuck', 'Magnificent app which corrects your previous console command.', 'github,auto', 'production', 'P2', 2.96, 95145, '2026-01-01', 'MONTH', '2026-01-12 00:00:12');
INSERT INTO `repo` VALUES (107, 'microsoft', 'Web-Dev-For-Beginners', 'microsoft/Web-Dev-For-Beginners', 'microsoft/Web-Dev-For-Beginners', '24 Lessons, 12 Weeks, Get Started as a Web Developer', 'github,auto', 'production', 'P2', 1.85, 94969, '2026-01-01', 'MONTH', '2026-01-12 00:00:12');
INSERT INTO `repo` VALUES (108, '2dust', 'v2rayN', '2dust/v2rayN', '2dust/v2rayN', 'A GUI client for Windows, Linux and macOS, support Xray and sing-box and others', 'github,auto', 'production', 'P2', 45.93, 94341, '2026-01-01', 'MONTH', '2026-01-12 00:00:12');
INSERT INTO `repo` VALUES (109, 'ryanmcdermott', 'clean-code-javascript', 'ryanmcdermott/clean-code-javascript', 'ryanmcdermott/clean-code-javascript', 'Clean Code concepts adapted for JavaScript', 'github,auto', 'production', 'P2', 1.57, 94236, '2026-01-01', 'MONTH', '2026-01-12 00:00:12');
INSERT INTO `repo` VALUES (110, 'fastapi', 'fastapi', 'fastapi/fastapi', 'fastapi/fastapi', 'FastAPI framework, high performance, easy to learn, fast to code, ready for production', 'github,auto', 'production', 'P2', 76.16, 93961, '2026-01-01', 'MONTH', '2026-01-12 00:00:12');
INSERT INTO `repo` VALUES (111, 'iluwatar', 'java-design-patterns', 'iluwatar/java-design-patterns', 'iluwatar/java-design-patterns', 'Design patterns implemented in Java', 'github,auto', 'production', 'P2', 7.12, 93601, '2026-01-01', 'MONTH', '2026-01-12 00:00:12');
INSERT INTO `repo` VALUES (112, 'puppeteer', 'puppeteer', 'puppeteer/puppeteer', 'puppeteer/puppeteer', 'JavaScript API for Chrome and Firefox', 'github,auto', 'production', 'P2', 41.87, 93251, '2026-01-01', 'MONTH', '2026-01-12 00:00:12');
INSERT INTO `repo` VALUES (1056, 'lishangye', 'OpenRank', 'lishangye/OpenRank', 'lishangye/OpenRank', '开源治理AI助手', 'github,manual', 'production', 'P2', 0, 11, '2026-01-01', 'MONTH', '2026-01-13 01:58:05');

-- ----------------------------
-- Table structure for repo_metric_month
-- ----------------------------
DROP TABLE IF EXISTS `repo_metric_month`;
CREATE TABLE `repo_metric_month`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `repo_id` bigint NOT NULL COMMENT '关联 repo.id',
  `period` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'yyyy-MM 月份',
  `openrank` double NULL DEFAULT NULL COMMENT 'OpenRank 月度值',
  `stars` bigint NULL DEFAULT NULL COMMENT 'Star 总数（当月快照）',
  `attention` double NULL DEFAULT NULL COMMENT '关注度（attention）',
  `activity` double NULL DEFAULT NULL COMMENT '活跃度（activity）',
  `bus_factor` double NULL DEFAULT NULL COMMENT '巴士系数（月度）',
  `bus_factor_detail` json NULL COMMENT '巴士系数明细（每人每月）',
  `participants` int NULL DEFAULT NULL COMMENT '项目参与者人数',
  `technical_fork` bigint NULL DEFAULT NULL COMMENT '技术 fork 数',
  `issues_new` int NULL DEFAULT NULL COMMENT '当月新建 issue 数',
  `issues_closed` int NULL DEFAULT NULL COMMENT '当月关闭 issue 数',
  `issue_comments` int NULL DEFAULT NULL COMMENT 'issue 评论数',
  `issue_age` double NULL DEFAULT NULL COMMENT 'issue 生命周期（天，开启到关闭）',
  `issue_response_time` double NULL DEFAULT NULL COMMENT 'issue 首次响应时长（天）',
  `issue_resolution_duration` double NULL DEFAULT NULL COMMENT 'issue 解决时长（天）',
  `change_requests` int NULL DEFAULT NULL COMMENT 'PR/变更请求数量',
  `change_requests_reviews` int NULL DEFAULT NULL COMMENT 'PR 审阅者数量',
  `change_request_age` double NULL DEFAULT NULL COMMENT 'PR 生命周期（天）',
  `change_request_response_time` double NULL DEFAULT NULL COMMENT 'PR 首次响应时长（天）',
  `change_request_resolution_duration` double NULL DEFAULT NULL COMMENT 'PR 解决时长（天）',
  `code_change_lines_add` bigint NULL DEFAULT NULL COMMENT '代码新增行数',
  `code_change_lines_remove` bigint NULL DEFAULT NULL COMMENT '代码删除行数',
  `code_change_lines_sum` bigint NULL DEFAULT NULL COMMENT '代码变更总行数',
  `inactive_contributors` int NULL DEFAULT NULL COMMENT '不活跃贡献者数量',
  `new_contributors` int NULL DEFAULT NULL COMMENT '新贡献者数量',
  `new_contributors_detail` json NULL COMMENT '新贡献者明细列表',
  `contributor_email_suffixes` json NULL COMMENT '贡献者邮箱后缀分布',
  `issues_and_change_request_active` int NULL DEFAULT NULL COMMENT 'issue+PR 活跃数量',
  `activity_details` json NULL COMMENT '每人每天活跃度明细（activity_details.json）',
  `active_dates_and_times` json NULL COMMENT '每日活跃度（active_dates_and_times.json）',
  `bus_factor_detail_json` json NULL COMMENT '巴士系数明细原始 JSON',
  `attention_json` json NULL COMMENT '关注度原始 JSON',
  `participants_json` json NULL COMMENT '参与者人数原始 JSON',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_repo_period`(`repo_id`, `period`) USING BTREE,
  INDEX `idx_period`(`period`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库按月指标快照' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repo_metric_month
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密存储）',
  `email` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'lishangye', '$2a$10$.1C4eX1we5MQQKz5ydiWLu41FsqBIH4nX2jMxltTpBR4uvAZosss6', '1905532344@qq.com', '2026-01-10 16:37:06', '2026-01-10 16:37:05');
INSERT INTO `user` VALUES (2, 'admin', '$2a$10$gElAHHSr97cRyK2byW/v7.vUjSEm8.TjCOhdNHjrEiuBx6wc2QzgW', '1905532344@qq.com', '2026-01-11 22:27:06', '2026-01-11 22:27:06');
INSERT INTO `user` VALUES (3, 'test', '$2a$10$gkOMjLVGoqyQ5Ji0Ko0meefoyI3GIQX5TMKc86psYO2zuowCNJREW', '123456', '2026-01-13 02:06:41', '2026-01-13 02:06:40');

SET FOREIGN_KEY_CHECKS = 1;
