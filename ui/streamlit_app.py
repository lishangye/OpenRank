import streamlit as st
import requests
import pandas as pd

# D:\python\python.exe -m streamlit run e:/比赛/OpenRank杯/ui/streamlit_app.py
# D:\python\python.exe -m uvicorn app.server:app --reload --port 8000


# Streamlit 页面基础配置
st.set_page_config(page_title="开源治理AI助手 MVP", layout="wide")
st.title("开源治理AI助手（MVP 演示）")

# 侧边栏：可配置后端地址与项目名称
api = st.sidebar.text_input("后端地址", "http://127.0.0.1:8000")
project = st.sidebar.text_input("项目（演示用）", "apache/iotdb")

# 示例问题，便于现场演示
st.markdown("### 请输入问题（示例）")
st.code("最近一个月活跃贡献者趋势怎么样？是否异常？给出治理建议")
st.code("Issue 首次响应时长怎么样？怎么优化 triage 流程？")
st.code("新贡献者留存不高，应该怎么做运营？")

q = st.text_input("你的问题")

if st.button("发送"):
    if not q.strip():
        st.warning("请输入问题")
    else:
        # 统一对话入口：发送到 FastAPI
        resp = requests.post(
            f"{api}/chat", json={"user_message": q, "project": project}, timeout=60
        )

        if not resp.ok:
            st.error(f"后端返回错误：{resp.status_code}")
            st.code(resp.text)
        else:
            try:
                data = resp.json()
            except ValueError:
                st.error("后端响应不是合法 JSON")
                st.code(resp.text)
                data = None

            if data:
                st.subheader("AI 回答")
                st.write(data.get("answer", ""))

                # 工具调用结果可展开查看，方便演示“系统调用了哪些能力”
                with st.expander("调试信息（工具调用）"):
                    st.json(data.get("used_tools", {}))

                # 展示 SQL 的摘要结果，辅助可视化
                sql = data.get("used_tools", {}).get("sql")
                if sql and "summary" in sql:
                    st.subheader("指标摘要（演示）")
                    st.table(pd.DataFrame([sql["summary"]]))
