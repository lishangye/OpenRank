import os
import sqlite3
import pandas as pd
from datetime import date, timedelta
import random

# SQLite 数据库路径
DB_PATH = os.path.join("data", "metrics.db")


def init_db() -> sqlite3.Connection:
    """初始化数据库与表结构。"""
    os.makedirs("data", exist_ok=True)
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    # 指标表：按天、项目、指标名、数值存储
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS metrics(
            day TEXT NOT NULL,
            project TEXT NOT NULL,
            metric TEXT NOT NULL,
            value REAL NOT NULL
        )
        """
    )
    conn.commit()
    return conn


def seed_demo_data(conn: sqlite3.Connection, project: str = "apache/iotdb", days: int = 120) -> None:
    """生成模拟 OpenDigger 指标数据，用于演示。"""
    start = date.today() - timedelta(days=days)
    rows = []

    # 模拟一个基础水平，再叠加随机波动
    active_base = 35
    issue_base = 8
    pr_base = 6

    for i in range(days):
        d = start + timedelta(days=i)

        # 模拟指标波动
        active = max(5, int(random.gauss(active_base, 6)))
        issues = max(0, int(random.gauss(issue_base, 3)))
        prs = max(0, int(random.gauss(pr_base, 2)))
        first_response_hours = max(0.5, random.gauss(18, 6))

        rows += [
            (d.isoformat(), project, "active_contributors", active),
            (d.isoformat(), project, "new_issues", issues),
            (d.isoformat(), project, "merged_prs", prs),
            (d.isoformat(), project, "issue_first_response_hours", first_response_hours),
        ]

        # 制造一个“异常”区间，方便演示异常检测与建议输出
        if 70 <= i <= 80:
            rows[-4] = (
                d.isoformat(),
                project,
                "active_contributors",
                max(3, int(active * 0.4)),
            )

    df = pd.DataFrame(rows, columns=["day", "project", "metric", "value"])
    df.to_sql("metrics", conn, if_exists="append", index=False)


if __name__ == "__main__":
    conn = init_db()

    # 清空演示数据并重新生成，便于重复演示
    conn.execute("DELETE FROM metrics")
    conn.commit()

    seed_demo_data(conn)
    conn.close()

    print("✅ 已生成演示数据：data/metrics.db")
