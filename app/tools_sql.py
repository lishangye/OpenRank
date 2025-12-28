import os
import sqlite3
from typing import Dict, Any, Tuple
import pandas as pd

# SQLite 数据库路径
DB_PATH = os.path.join("data", "metrics.db")


def _conn() -> sqlite3.Connection:
    """获取数据库连接。"""
    return sqlite3.connect(DB_PATH)


def query_metric_timeseries(project: str, metric: str, days: int = 30) -> pd.DataFrame:
    """查询指定指标的最近 N 天时间序列。"""
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
