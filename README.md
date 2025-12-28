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
