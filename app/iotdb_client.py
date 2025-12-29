import os
import time
from typing import Optional

import pandas as pd


class IoTDBClient:
    """IoTDB 查询封装，按 OpenSODA 指标规范返回统一结构。"""

    def __init__(self):
        # IoTDB 连接信息：使用环境变量，便于部署与切换
        self.host = os.getenv("IOTDB_HOST", "127.0.0.1")
        self.port = int(os.getenv("IOTDB_PORT", "6667"))
        self.user = os.getenv("IOTDB_USER", "root")
        self.password = os.getenv("IOTDB_PASSWORD", "root")

        # 指标设备路径：root.opensoda.project.{project}
        self.device_root = os.getenv("IOTDB_DEVICE_ROOT", "root.opensoda.project")

    def _sanitize_project(self, project: str) -> str:
        """将项目名转换为 IoTDB 设备可用的路径片段。"""
        return (
            project.replace("/", "_")
            .replace("-", "_")
            .replace(".", "_")
            .replace(" ", "_")
        )

    def _device_path(self, project: str) -> str:
        """拼接完整设备路径。"""
        return f"{self.device_root}.{self._sanitize_project(project)}"

    def _open_session(self):
        """延迟导入并创建 Session，避免未安装客户端时报错。"""
        try:
            from iotdb.Session import Session  # type: ignore
        except Exception as exc:  # noqa: BLE001
            raise RuntimeError(
                "未安装 iotdb-client，请先执行：pip install iotdb-client"
            ) from exc

        session = Session(self.host, self.port, self.user, self.password)
        session.open(False)
        return session

    def query_metric_timeseries(self, project: str, metric: str, days: int = 30) -> pd.DataFrame:
        """查询最近 N 天的指标时间序列，返回 day/value 两列。"""
        device = self._device_path(project)
        start_ms = int((time.time() - days * 86400) * 1000)

        sql = f"select {metric} from {device} where time >= {start_ms} order by time asc"

        session = self._open_session()
        try:
            dataset = session.execute_query_statement(sql)
            try:
                df = dataset.todf()
            except Exception as exc:  # noqa: BLE001
                raise RuntimeError(
                    "IoTDB Python 客户端不支持 todf()，请升级 iotdb-client 版本"
                ) from exc
        finally:
            session.close()

        # 兼容列名：Time / full path
        if df.empty:
            return pd.DataFrame(columns=["day", "value"])

        # 找到时间列
        time_col = "Time" if "Time" in df.columns else df.columns[0]
        # 值列通常是完整路径
        value_col = None
        for c in df.columns:
            if c != time_col:
                value_col = c
                break
        if value_col is None:
            return pd.DataFrame(columns=["day", "value"])

        out = pd.DataFrame()
        out["day"] = pd.to_datetime(df[time_col], unit="ms").dt.date.astype(str)
        out["value"] = pd.to_numeric(df[value_col], errors="coerce")
        out = out.dropna(subset=["value"])
        return out.sort_values("day")
