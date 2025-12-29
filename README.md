# OpenSODA Governance AI MVP

这是一个最小可用的开源治理 AI 助手演示项目，包含三类能力：

1. 问数（SQL）：自然语言 → 指标查询（SQLite 演示库）
2. 知识库问答（RAG）：从 `docs/` 中检索治理文档
3. 统一对话入口：综合数据结果和文档证据，生成治理建议

## 快速开始

### 1) 安装依赖

```bash
pip install -r requirements.txt
```

### 2) 设置千问 Key（DASHSCOPE_API_KEY）

Windows PowerShell:

```powershell
setx DASHSCOPE_API_KEY "你的key"
```

### 3) 生成演示数据

```bash
python app/seed_data.py
```

### 4) 启动后端

```bash
uvicorn app.server:app --reload --port 8000
```

### 5) 启动前端

```bash
streamlit run ui/streamlit_app.py
```

## 目录结构

```
opensoda-governance-ai/
  app/
    server.py              # FastAPI 后端（聊天接口）
    qwen_client.py         # 千问调用封装
    rag.py                 # 简单RAG（TF-IDF检索）
    tools_sql.py           # “问数”工具（SQLite）
    seed_data.py           # 生成演示数据
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

## 备注

- 本项目使用阿里千问（Qwen）OpenAI 兼容接口（base_url + api_key）。
- 当前使用 SQLite 与 TF-IDF 作为演示实现，后续可替换为 IoTDB / MaxKB / SQLBot 等。
- 若使用 IoTDB，请先安装 Python 客户端并配置环境变量：

```bash
pip install iotdb-client==1.1.0
```

```powershell
setx USE_IOTDB "1"
setx IOTDB_HOST "127.0.0.1"
setx IOTDB_PORT "6667"
setx IOTDB_USER "root"
setx IOTDB_PASSWORD "root"
setx IOTDB_DEVICE_ROOT "root.opensoda.project"
```

数据路径规则（默认）：
- 设备：`root.opensoda.project.{project}`（例如 `apache/iotdb` -> `root.opensoda.project.apache_iotdb`）
- 指标测点：`active_contributors`、`new_issues`、`merged_prs`、`issue_first_response_hours`

## DataEase 集成（预留）

DataEase 作为可视化层，本项目预留了接口与配置说明，部署完成后即可联通。

推荐版本：DataEase 最新稳定版（2.x）。

### 1) 基本配置（本地 8081）

```powershell
setx DATAEASE_BASE_URL "http://127.0.0.1:8081"
setx DATAEASE_TOKEN "你的Token（如需）"
setx DATAEASE_TOKEN_HEADER "Authorization"
```

后端提供探测接口（可在 DataEase 部署后验证可达性）：

```
GET http://127.0.0.1:8000/dataease/health
```

### 2) DataEase 数据集建议（基于 IoTDB）

在 DataEase 中创建 IoTDB 数据源，并建立数据集（示例查询）：

```
SELECT time, active_contributors, new_issues, merged_prs, issue_first_response_hours
FROM root.opensoda.project.apache_iotdb
```

说明：
- 可在 DataEase 中配置时间过滤与聚合（如按天聚合）
- 项目名可按需替换为你的设备路径

### 3) 图表建议（符合比赛要求）

- 指标柱状图：X 轴为时间，Y 轴为 `active_contributors` / `new_issues` / `merged_prs`
- 指标表格：列包含 `time` 与各指标值，用于展示明细与对比
