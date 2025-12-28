import os
from typing import List, Dict, Any

import requests

# 阿里千问 OpenAI 兼容接口（国内区域）
# 如需国际站，请改为：https://dashscope-intl.aliyuncs.com/compatible-mode/v1
BASE_URL_CN = "https://dashscope.aliyuncs.com/compatible-mode/v1"


class QwenClient:
    """千问调用封装：统一处理 base_url 和 API Key。"""

    def __init__(self, model: str = "qwen-plus", base_url: str = BASE_URL_CN):
        # 从环境变量读取密钥，避免硬编码
        # api_key = os.getenv("DASHSCOPE_API_KEY")
        # if not api_key:
        #     raise RuntimeError("请先设置环境变量 DASHSCOPE_API_KEY")
        api_key = "sk-f0b61889e6f945f7a68447c7571b7914"

        # 仅保留必要信息，调用时按 OpenAI 兼容协议手动发请求
        self.api_key = api_key
        self.model = model
        self.base_url = base_url.rstrip("/")

    def chat(
        self,
        messages: List[Dict[str, Any]],
        temperature: float = 0.2,
        max_tokens: int = 700,
    ) -> str:
        """封装聊天调用，返回模型文本内容（OpenAI 兼容 /chat/completions）。"""
        url = f"{self.base_url}/chat/completions"
        payload = {
            "model": self.model,
            "messages": messages,
            "temperature": temperature,
            "max_tokens": max_tokens,
        }
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
        }

        resp = requests.post(url, json=payload, headers=headers, timeout=60)
        if not resp.ok:
            raise RuntimeError(f"LLM 请求失败：{resp.status_code} {resp.text}")

        data = resp.json()
        try:
            return data["choices"][0]["message"]["content"]
        except (KeyError, IndexError, TypeError) as exc:
            raise RuntimeError(f"LLM 响应结构异常：{data}") from exc
