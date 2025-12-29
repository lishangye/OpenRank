import os
from typing import Dict, Any

import requests


class DataEaseClient:
    """DataEase 连接封装（预留集成，不影响现有功能）。"""

    def __init__(self):
        # DataEase 本地默认地址
        self.base_url = os.getenv("DATAEASE_BASE_URL", "http://127.0.0.1:8081").rstrip("/")
        # 可选 Token，用于后续对接 API
        self.token = os.getenv("DATAEASE_TOKEN", "")
        # 自定义 Header 名称，默认使用 Authorization: Bearer <token>
        self.token_header = os.getenv("DATAEASE_TOKEN_HEADER", "Authorization")

    def _headers(self) -> Dict[str, str]:
        """构造请求头，若未配置 token 则返回空头。"""
        if not self.token:
            return {}
        if self.token_header.lower() == "authorization":
            return {"Authorization": f"Bearer {self.token}"}
        return {self.token_header: self.token}

    def health_check(self) -> Dict[str, Any]:
        """
        尝试探测 DataEase 是否可达。
        注意：不同版本接口路径可能不同，这里仅做轻量探测。
        """
        candidates = ["/api/health", "/health", "/"]
        for path in candidates:
            url = f"{self.base_url}{path}"
            try:
                resp = requests.get(url, headers=self._headers(), timeout=5)
                return {
                    "url": url,
                    "status_code": resp.status_code,
                    "ok": resp.ok,
                    "text": resp.text[:200],
                }
            except requests.RequestException as exc:
                last_error = str(exc)
        return {"url": self.base_url, "ok": False, "error": last_error}
