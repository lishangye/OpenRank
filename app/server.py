from fastapi import FastAPI
from pydantic import BaseModel
from typing import Dict, Any

from app.qwen_client import QwenClient
from app.rag import SimpleRAG
from app.tools_sql import query_metric_summary, simple_anomaly_hint

app = FastAPI(title="OpenSODA 开源治理AI助手 MVP")

# 初始化模型与检索器
qwen = QwenClient(model="qwen-plus")
rag = SimpleRAG(docs_dir="docs")
rag.build()

DEFAULT_PROJECT = "apache/iotdb"


class ChatReq(BaseModel):
    """聊天请求结构。"""
    user_message: str
    project: str | None = None


class ChatResp(BaseModel):
    """聊天响应结构，包含答案和工具调用明细。"""
    answer: str
    used_tools: Dict[str, Any]


def route_intent(text: str) -> str:
    """
    最小化意图路由：
    - data: 倾向查询指标
    - kb: 倾向检索知识库
    - hybrid: 两者结合
    """
    data_kw = ["多少", "趋势", "平均", "最近", "活跃", "贡献者", "issue", "pr", "响应", "时长", "异常"]
    kb_kw = ["怎么", "如何", "规范", "流程", "贡献指南", "治理", "triage", "review", "mentor", "导师"]

    has_data = any(k.lower() in text.lower() for k in data_kw)
    has_kb = any(k.lower() in text.lower() for k in kb_kw)

    if has_data and has_kb:
        return "hybrid"
    if has_data:
        return "data"
    if has_kb:
        return "kb"
    return "hybrid"


@app.post("/chat", response_model=ChatResp)
def chat(req: ChatReq) -> ChatResp:
    """统一对话入口：先查数/检索，再交给大模型生成建议。"""
    project = req.project or DEFAULT_PROJECT
    user_q = req.user_message.strip()

    used = {"sql": None, "rag": None}
    intent = route_intent(user_q)

    # 1) SQL 问数
    sql_context = ""
    if intent in ("data", "hybrid"):
        # 简单关键词映射到指标
        metric_map = [
            ("活跃", "active_contributors"),
            ("贡献者", "active_contributors"),
            ("issue", "new_issues"),
            ("问题", "new_issues"),
            ("pr", "merged_prs"),
            ("合并", "merged_prs"),
            ("响应", "issue_first_response_hours"),
            ("时长", "issue_first_response_hours"),
        ]
        metric = "active_contributors"
        for k, m in metric_map:
            if k.lower() in user_q.lower():
                metric = m
                break

        summary = query_metric_summary(project, metric, days=30)
        abnormal, hint = simple_anomaly_hint(project, metric, days=30)
        used["sql"] = {"metric": metric, "summary": summary, "anomaly": abnormal, "hint": hint}

        sql_context = (
            f"【数据指标】项目={project}\n"
            f"metric={metric}\n"
            f"近30天均值={summary.get('avg'):.2f}，最新值={summary.get('latest_value'):.2f}（{summary.get('latest_day')}）\n"
            f"min={summary.get('min'):.2f}，max={summary.get('max'):.2f}\n"
            f"【异常判断】{hint}\n"
        )

    # 2) RAG 知识库检索
    rag_context = ""
    if intent in ("kb", "hybrid"):
        chunks = rag.retrieve(user_q, topk=3)
        used["rag"] = [
            {"source": c.source, "text": c.text[:240] + ("..." if len(c.text) > 240 else "")}
            for c in chunks
        ]
        if chunks:
            rag_context = "【知识库检索】\n" + "\n\n".join(
                [f"- 来源:{c.source}\n{c.text}" for c in chunks]
            )

    # 3) 统一提示词：要求模型严格基于证据给出建议
    system = (
        "你是“开源治理与运营AI助手”。\n"
        "你必须基于给定的【数据指标】与【知识库检索】回答。\n"
        "输出结构要求：\n"
        "1) 结论（1-2句）\n"
        "2) 依据（引用数据/文档要点）\n"
        "3) 建议动作（给出可执行的治理/运营动作清单，3-6条）\n"
        "语言：中文，简洁专业。"
    )

    messages = [
        {"role": "system", "content": system},
        {"role": "user", "content": f"用户问题：{user_q}\n\n{sql_context}\n\n{rag_context}"},
    ]
    answer = qwen.chat(messages)

    return ChatResp(answer=answer, used_tools=used)
