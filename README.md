
# OpenSODA Governance AI Assistant (MVP)

面向 **“开源治理与运营”** 场景的 **大模型（LLM）+ 数据分析 + 知识检索（RAG）** 创新应用。
本项目为 OpenSODA “OpenRank杯”开源数字生态分析与应用创新赛的参赛方案/原型（MVP），目标是用 **自然语言交互** 打通开源治理日常工作中的“看数、看文档、做决策”链路，提升治理效率与社区体验。
（对应赛项作品方向：**面向开源治理与运营的大模型与人工智能应用作品**）

---

## 1. 项目定位与价值

### 1.1 解决的治理与运营痛点

开源项目维护者/OSPO 往往面临：

* **信息分散**：指标在不同平台/报表，文档和历史讨论在不同仓库/issue/群聊
* **问数门槛高**：很多治理相关问题需要 SQL/脚本才能回答
* **洞察滞后**：活跃度异常、Issue 堆积、响应变慢等风险无法及时预警
* **决策难形成闭环**：看到了数据，却不知道“下一步做什么”

### 1.2 方案核心价值

* **从“数据可视化”升级为“智能治理决策”**：不仅展示数据，还自动解释原因并给出行动建议
* **降低治理门槛**：用一句中文就能得到“指标结果 + 文档证据 + 建议动作”
* **可迁移/可扩展**：可对接 OpenDigger 指标、IoTDB 时序存储、MaxKB 知识库、SQLBot 问数、DataEase 可视化

---

## 2. 功能概览

本项目最小可用原型包含三类能力：

1. **问数（NL→指标查询）**

* 自然语言 → 指标意图识别 → 查询指标库（MVP 用 SQLite 演示）
* 输出：近 30 天统计、趋势摘要、简单异常提示

2. **知识库问答（RAG）**

* 从 `docs/` 的治理/运营资料中检索证据
* 输出：引用来源 + 关键段落（可替换为 MaxKB）

3. **统一对话入口（LLM 决策建议）**

* 将 **数据证据 + 文档证据** 作为上下文喂给千问 Qwen
* 输出结构固定：**结论 → 依据 → 建议动作（3~6条）**

> MVP 演示重点：评委输入一句话 → 系统自动查数/查文档 → 千问输出治理建议与可执行动作清单。

---

## 3. 技术方案与架构

### 3.1 总体架构（分层+插件化）

* **数据层**：OpenDigger / GitHub(Gitee) 开放数据 → 指标计算与落库（MVP: SQLite；升级: IoTDB）
* **知识层**：治理文档/README/Issue 讨论 → 知识库（MVP: docs+TFIDF；升级: MaxKB）
* **AI 编排层**：意图识别 → 工具调用（SQL/RAG/图分析预留）→ 上下文组装 → LLM 输出
* **应用层**：Streamlit 演示前端（升级: DataEase Dashboard + Web 前端）

### 3.2 关键创新点

* **“证据驱动”的治理智能体**：LLM 不凭空回答，必须引用指标结果/文档证据，再生成建议（降低幻觉）
* **治理决策模板化输出**：结论—依据—行动清单，贴近维护者工作方式，可直接执行
* **多工具链协同**：SQLBot（问数）+ MaxKB（知识）+ IoTDB（时序）+ DataEase（可视化）可组合扩展

---

## 4. 与赛项指定开源工具集的关联方式

本方案面向赛项要求：参赛作品需从指定工具集中至少选用一个项目并做创新应用【赛项简介：关联开源项目与参赛要求】。

### 4.1 当前 MVP 采用方式

* **LLM**：阿里千问 Qwen（OpenAI 兼容接口）
* **问数**：IoTDB 演示指标库
* **RAG**：docs + TF-IDF（对标 MaxKB 的知识库能力）
* **可视化**：DataEase 演示

### 4.2 升级对接路径（对应复赛/决赛实现）

* OpenDigger：作为指标数据/数据集来源（采集+指标计算）
* IoTDB：作为时序指标存储与趋势分析引擎（替换 SQLite）
* MaxKB：作为生产级知识库与向量检索平台（替换 TF-IDF RAG）
* SQLBot：作为 NL2SQL 问数能力（替换手工规则/简单解析）
* DataEase：作为治理仪表盘与大屏展示（替换 Streamlit 或并行展示）
* EasyGraph：用于开发者协作网络/关键贡献者影响力图分析（本 MVP 预留扩展接口）

---

## 5. 快速开始（MVP 运行）

### 5.1 安装依赖

```bash
pip install -r requirements.txt
```

### 5.2 设置千问 Key（DASHSCOPE_API_KEY）

Windows PowerShell:

```powershell
setx DASHSCOPE_API_KEY "你的key"
```

### 5.3 生成演示数据（模拟开源项目时序指标）

```bash
python app/seed_data.py
```

### 5.4 启动后端（FastAPI）

```bash
uvicorn app.server:app --reload --port 8000
```

### 5.5 启动前端（Streamlit）

```bash
streamlit run ui/streamlit_app.py
```

---

## 6. 演示脚本

### 6.1 Demo 问题 1：活跃度异常 + 治理建议

输入：

> 最近一个月活跃贡献者趋势怎么样？是否异常？给出治理建议。

期望输出：

* 结论：活跃度下滑（或无异常）
* 依据：近30天均值/最近值/异常提示
* 建议动作：发布节奏、review 分担、mentor 机制、贡献指南优化等

### 6.2 Demo 问题 2：Issue 响应变慢 + triage 优化

输入：

> Issue 首次响应时长怎么样？怎么优化 triage 流程？

期望输出：

* 结论：首响变慢（或稳定）
* 依据：首响统计 + 文档中 triage 建议段落
* 建议动作：标签体系、轮值、SLA、模板化引导等

### 6.3 Demo 问题 3：新贡献者留存（运营）

输入：

> 新贡献者留存不高，应该怎么做运营？

期望输出：

* 结论：留存风险（基于文档策略+可扩展数据）
* 建议动作：good first issue、导师制、贡献路径、入门脚本等

---

## 7. 数据模型与指标定义（面向“治理与运营”）

### 7.1 指标（MVP）

* `active_contributors`：活跃贡献者数
* `new_issues`：新增 Issue 数
* `merged_prs`：合并 PR 数
* `issue_first_response_hours`：Issue 首次响应时长（小时）

> 上述指标可从 OpenDigger/平台数据映射得到，符合“开源社区健康度与运营”常见分析口径。

### 7.2 SQLite 表结构（演示）

`metrics(day, project, metric, value)`

---

## 8. IoTDB 支持（可选启用，面向复赛升级）

> MVP 默认使用 SQLite。若启用 IoTDB，可将时序指标落到 IoTDB，并由问数模块查询 IoTDB。

### 8.1 安装 IoTDB Python 客户端

```bash
pip install iotdb-client==1.1.0
```

### 8.2 环境变量（示例）

```powershell
setx USE_IOTDB "1"
setx IOTDB_HOST "127.0.0.1"
setx IOTDB_PORT "6667"
setx IOTDB_USER "root"
setx IOTDB_PASSWORD "root"
setx IOTDB_DEVICE_ROOT "root.opensoda.project"
```

### 8.3 数据路径约定

* 设备：`root.opensoda.project.{project}`

  * 例：`apache/iotdb` → `root.opensoda.project.apache_iotdb`
* 测点：`active_contributors`、`new_issues`、`merged_prs`、`issue_first_response_hours`

---

## 9. DataEase 集成

DataEase 作为可视化/报表层，可提供“治理大屏”和“项目对比面板”。
本项目预留探测接口与配置说明，部署 DataEase 后即可联通。

### 9.1 基本配置

```powershell
setx DATAEASE_BASE_URL "http://127.0.0.1:8081"
setx DATAEASE_TOKEN "你的Token（如需）"
setx DATAEASE_TOKEN_HEADER "Authorization"
```

后端探测接口（可用于部署验证）：

```
GET http://127.0.0.1:8000/dataease/health
```

### 9.2 DataEase 数据集建议（基于 IoTDB）

示例查询：

```sql
SELECT time, active_contributors, new_issues, merged_prs, issue_first_response_hours
FROM root.opensoda.project.apache_iotdb
```

### 9.3 图表建议

* 趋势折线：活跃贡献者 / 首响时长（可叠加均线）
* 运营柱状：新增 Issue、合并 PR（按周/按月）
* 指标表格：近 30 天明细 + 异常标记
* 多项目对比：同类指标横向对比（OSPO 场景加分）

---

## 10. 目录结构

```text
opensoda-governance-ai/
  app/
    server.py              # FastAPI 后端（聊天接口）
    qwen_client.py         # 千问调用封装（OpenAI compatible）
    rag.py                 # 简单RAG（TF-IDF检索）-> 可替换 MaxKB
    tools_sql.py           # “问数”工具（SQLite）-> 可替换 SQLBot/IoTDB
    seed_data.py           # 生成演示数据（模拟开源社区指标）
  docs/
    governance_guide.md    # 开源治理/运营知识
    chaoss_metrics.md      # 社区健康指标
  ui/
    streamlit_app.py       # 演示前端（聊天 + 指标展示）
  data/
    metrics.db             # SQLite 数据库（自动生成）
  requirements.txt
  README.md
```


