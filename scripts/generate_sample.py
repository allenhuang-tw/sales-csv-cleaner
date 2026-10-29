#!/usr/bin/env python3
"""產生虛構的雜亂銷售 CSV(sample/sales_messy.csv),固定亂數種子,每次結果相同。"""
import csv
import random
from datetime import date, timedelta
from pathlib import Path

random.seed(20261029)
PRODUCTS = {"珍珠奶茶": 55, "無線滑鼠": 590, "機械鍵盤": 2490, "保溫瓶": 880,
            "藍牙耳機": 1290, "筆記本": 45, "行動電源": 750, "咖啡豆(半磅)": 420}

def fmt_date(d):
    style = random.choice(["slash", "dash", "us", "slash_short", "dot"])
    return {"slash": d.strftime("%Y/%m/%d"), "dash": d.strftime("%Y-%m-%d"),
            "us": d.strftime("%m/%d/%Y"), "slash_short": f"{d.year}/{d.month}/{d.day}",
            "dot": d.strftime("%Y.%m.%d")}[style]

def fmt_money(v):
    style = random.choice(["plain", "comma", "dollar", "nt", "nt_space"])
    return {"plain": str(v), "comma": f"{v:,}", "dollar": f"${v:,}",
            "nt": f"NT${v:,}", "nt_space": f"NT$ {v:,}"}[style]

rows = []
start = date(2026, 7, 1)
for _ in range(178):
    p = random.choice(list(PRODUCTS))
    d = start + timedelta(days=random.randint(0, 121))   # 2026-07 ~ 2026-10
    qty = random.randint(1, 12)
    price = PRODUCTS[p] + random.choice([0, 0, 0, -10, 10])
    qty_s = f"{qty:,}" if qty < 1000 else f"{qty:,}"
    rows.append([fmt_date(d), random.choice([p, p, f" {p} "]), qty_s, fmt_money(price)])

# 刻意加入無效列
rows += [
    ["", "無線滑鼠", "2", "590"],                 # 日期空白
    ["2026/13/40", "保溫瓶", "1", "880"],         # 日期不存在
    ["2026/08/15", "", "3", "55"],                # 商品空白
    ["2026-09-02", "筆記本", "", "45"],           # 數量空白
    ["2026-09-03", "行動電源", "兩個", "750"],     # 數量非數字
    ["2026/10/11", "藍牙耳機", "1", ""],          # 單價空白
    ["2026/10/12", "機械鍵盤", "1", "免費"],       # 單價無效
    ["", "", "", ""],                             # 整列空白
    ["", "", "", ""],
    ["2026-07-20", "珍珠奶茶", "-2", "55"],        # 負數量
]
# 刻意加入完全重複列
for r in random.sample(rows[:178], 12):
    rows.insert(random.randint(0, len(rows)), list(r))

out = Path(__file__).resolve().parent.parent / "sample" / "sales_messy.csv"
with out.open("w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["訂單日期", "商品", "數量", "單價"])
    w.writerows(rows)
print(f"wrote {len(rows)} data rows to {out}")
