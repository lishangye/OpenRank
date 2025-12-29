import os
import sqlite3
from typing import Dict, Any, Tuple

import pandas as pd

from app.iotdb_client import IoTDBClient

# SQLite 数据库路径（演示用）
DB_PATH = os.path.join("data", "metrics.db")
_iotdb_client = None


def _use_iotdb() -> bool:
    """根据环境变量决定是否启用 IoTDB。"""
    if os.getenv("USE_IOTDB", "").lower() in ("1", "true", "yes"):
        return True
    # 只要配置了 IOTDB_HOST，也视为启用
    return bool(os.getenv("IOTDB_HOST"))


def _get_iotdb_client() -> IoTDBClient:
    """延迟初始化 IoTDBClient，避免启动时依赖失败。"""
    global _iotdb_client
    if _iotdb_client is None:
        _iotdb_client = IoTDBClient()
    return _iotdb_client


def _conn() -> sqlite3.Connection:
    """获取数据库连接。"""
    return sqlite3.connect(DB_PATH)


def query_metric_timeseries(project: str, metric: str, days: int = 30) -> pd.DataFrame:
    """查询指定指标的最近 N 天时间序列。"""
    if _use_iotdb():
        # 优先走 IoTDB
        return _get_iotdb_client().query_metric_timeseries(project, metric, days)

    # 回退到 SQLite 演示库
    sql = """
    SELECT day, value FROM metrics
    WHERE project=? AND metric=?
    ORDER BY day DESC
    LIMIT ?
    """
    conn = _conn()
    df = pd.read_sql_query(sql, conn, params=(project, metric, days))
    conn.close()
    return df.sort_values("day")


def query_metric_summary(project: str, metric: str, days: int = 30) -> Dict[str, Any]:
    """返回指标的摘要统计信息，便于直接展示或写入提示词。"""
    df = query_metric_timeseries(project, metric, days)
    if df.empty:
        return {"metric": metric, "days": days, "count": 0}

    return {
        "metric": metric,
        "days": days,
        "count": int(df.shape[0]),
        "latest_day": df["day"].iloc[-1],
        "latest_value": float(df["value"].iloc[-1]),
        "avg": float(df["value"].mean()),
        "min": float(df["value"].min()),
        "max": float(df["value"].max()),
    }


def simple_anomaly_hint(project: str, metric: str, days: int = 30) -> Tuple[bool, str]:
    """
    简易异常检测：
    - 最近 7 天均值 < 前 23 天均值 * 0.7 -> 判定为显著下滑
    """
    df = query_metric_timeseries(project, metric, days)
    if df.shape[0] < 30:
        return False, "数据不足，无法判断异常。"

    last7 = df.tail(7)["value"].mean()
    prev = df.head(23)["value"].mean()
    if prev <= 0:
        return False, "历史基线为0，无法判断异常。"

    if last7 < prev * 0.7:
        return True, f"检测到异常：最近7天均值({last7:.2f})显著低于此前基线({prev:.2f})。"
    return False, f"未检测到明显异常：最近7天均值({last7:.2f})，此前基线({prev:.2f})。"
