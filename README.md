# Sales CSV Cleaner

把雜亂的銷售 CSV 一鍵清洗,並輸出成整理好的 Excel 報表(Java 17 + Apache POI)。
Clean a messy sales CSV and export a tidy Excel report in one command (Java 17 + Apache POI).

> 範例資料皆為虛構。 All sample data is fictional.

---

## 中文說明

### 解決什麼問題
從 POS、電商後台或手動登打匯出的銷售 CSV 常常很亂:重複列、空白欄、日期格式混用(`2026/10/05`、`2026-10-05`、`10/05/2026`)、金額帶 `$`、`NT$` 和千分位逗號。每次都要人工在 Excel 整理很花時間又容易出錯。這個工具用一行指令完成清洗與「商品 × 月份」彙總,直接產出可交給主管或客戶的 Excel。

### 功能
- 去除**完全重複**的列(前後空白不影響比對)
- 略過並列出**空白或無效列**(日期無法辨識、商品空白、數量非正整數、單價無效),附行號與原因
- **統一日期格式**:支援 `2026/10/05`、`2026/10/5`、`2026-10-05`、`2026.10.05`、`20261005`、`10/05/2026`(斜線在後的視為 月/日/年)
- **金額清洗**:移除 `$`、`NT$`、`US$`、`元`、千分位逗號與空白
- 依**商品 × 月份**彙總數量與銷售總額(金額 = 數量 × 單價),並附合計列
- 輸出 `.xlsx`,含「清洗後明細」「彙總」兩個工作表:表頭加粗、金額千分位、欄寬自動調整(中文字以雙倍寬度估算)、凍結表頭
- 檔案不存在、欄位缺少時顯示清楚的中文錯誤訊息;完成後印出處理統計
- 表頭可用中文(訂單日期、商品、數量、單價)或英文(Date、Product、Qty、Unit Price)

### 建置與執行
需要 JDK 17 以上與 Maven 3.8 以上。

```bash
mvn package                      # 編譯、跑單元測試、打包成 target/sales-csv-cleaner.jar
java -jar target/sales-csv-cleaner.jar sample/sales_messy.csv sample/output.xlsx
```

結束代碼:`0` 成功、`1` 參數錯誤、`2` 輸入檔問題(不存在/缺欄位)、`3` 無法寫入輸出檔。
重新產生範例資料:`python3 scripts/generate_sample.py`。

---

## English

### What problem it solves
Sales CSVs exported from POS systems, e-commerce dashboards or hand-typed sheets are often messy: duplicated rows, blank cells, mixed date formats, and amounts with `$` / `NT$` and thousands separators. Cleaning them by hand in Excel is slow and error-prone. This tool cleans the file and summarizes sales by *product × month* with a single command, producing an Excel report ready to share.

### Features
- Removes exact duplicate rows (ignoring leading/trailing spaces)
- Skips and reports blank/invalid rows (bad date, empty product, non-positive quantity, invalid price) with line numbers and reasons
- Normalizes dates: `2026/10/05`, `2026/10/5`, `2026-10-05`, `2026.10.05`, `20261005`, `10/05/2026` (treated as MM/DD/YYYY)
- Cleans amounts: strips `$`, `NT$`, `US$`, `元`, commas and spaces
- Summarizes quantity and revenue by product × month (amount = quantity × unit price) with a grand-total row
- Writes `.xlsx` with two sheets, "清洗後明細" (cleaned details) and "彙總" (summary): bold headers, thousands-separated numbers, auto-fitted column widths, frozen header row
- Clear error messages (in Chinese) for missing files or columns; prints processing statistics
- Accepts Chinese or English headers (Date, Product, Qty, Unit Price)

### Build & run
Requires JDK 17+ and Maven 3.8+.

```bash
mvn package        # compile, run unit tests, build target/sales-csv-cleaner.jar (fat jar)
java -jar target/sales-csv-cleaner.jar sample/sales_messy.csv sample/output.xlsx
```

---

## 輸入輸出範例 / Example

### 輸入 Input (`sample/sales_messy.csv`, 200 data rows)
```csv
訂單日期,商品,數量,單價
2026/7/29,咖啡豆(半磅),11,420
2026.07.28,筆記本,9,NT$ 45
09/27/2026, 咖啡豆(半磅) ,7,$420
2026.08.24,機械鍵盤,11,"$2,490"
2026-10-26,筆記本,10,$45
2026-07-26,藍牙耳機,8,"NT$ 1,280"
...
,無線滑鼠,2,590
2026/13/40,保溫瓶,1,880
2026/08/15,,3,55
```

### 命令列輸出 Console output
```text
=== Sales CSV Cleaner 處理統計 ===
讀入資料列:200 列(不含表頭)
去除重複列:12 列
無效列(已略過):10 列
有效明細:178 列
彙總(商品×月份):32 列
--- 無效列明細 ---
  第 191 行:日期空白
  第 192 行:日期格式無法辨識「2026/13/40」
  第 193 行:商品空白
  第 194 行:數量空白
  第 195 行:數量無效「兩個」
  第 197 行:單價空白
  第 198 行:單價無效「免費」
  第 199 行:空白列
  第 200 行:空白列
  第 201 行:數量無效「-2」
已輸出:.../sample/output.xlsx
```

### 工作表「清洗後明細」 Sheet: cleaned details (first rows)
| 原始行號 | 訂單日期 | 月份 | 商品 | 數量 | 單價 | 金額 |
|---:|---|---|---|---:|---:|---:|
| 2 | 2026-07-29 | 2026-07 | 咖啡豆(半磅) | 11 | 420 | 4,620 |
| 3 | 2026-07-28 | 2026-07 | 筆記本 | 9 | 45 | 405 |
| 4 | 2026-09-27 | 2026-09 | 咖啡豆(半磅) | 7 | 420 | 2,940 |
| 5 | 2026-08-24 | 2026-08 | 機械鍵盤 | 11 | 2,490 | 27,390 |
| 6 | 2026-10-26 | 2026-10 | 筆記本 | 10 | 45 | 450 |
| 7 | 2026-07-26 | 2026-07 | 藍牙耳機 | 8 | 1,280 | 10,240 |

### 工作表「彙總」 Sheet: summary (first rows + total)
| 商品 | 月份 | 總數量 | 銷售總額 |
|---|---|---:|---:|
| 行動電源 | 2026-07 | 18 | 13,320 |
| 行動電源 | 2026-08 | 30 | 22,560 |
| 行動電源 | 2026-09 | 59 | 44,230 |
| 行動電源 | 2026-10 | 36 | 27,100 |
| 咖啡豆(半磅) | 2026-07 | 26 | 10,970 |
| 咖啡豆(半磅) | 2026-08 | 45 | 18,830 |
| … | | | |
| **合計** | | **1,181** | **896,135** |

### 錯誤訊息範例 Error examples
```text
$ java -jar target/sales-csv-cleaner.jar nope.csv out.xlsx
錯誤:找不到輸入檔案「nope.csv」,請確認路徑是否正確。

$ java -jar target/sales-csv-cleaner.jar bad.csv out.xlsx
錯誤:CSV 缺少必要欄位:數量、單價。
目前的表頭是:日期, 商品
必要欄位:訂單日期、商品、數量、單價(也接受英文 Date, Product, Qty, Unit Price)。
```

---

## 專案結構 Project structure
```text
├── pom.xml                       Maven(Java 17、Apache POI、JUnit 5、maven-shade-plugin)
├── src/main/java/com/example/salescleaner/
│   ├── Main.java                 命令列入口、錯誤訊息、統計輸出
│   ├── CsvReader.java            CSV 讀取(支援引號欄位、BOM)
│   ├── DateParser.java           混合日期格式解析
│   ├── AmountParser.java         金額/數量清洗
│   ├── SalesCleaner.java         表頭檢查、去重、驗證
│   ├── Aggregator.java           商品 × 月份彙總
│   ├── ExcelWriter.java          Apache POI 輸出 xlsx
│   └── SalesRecord / SummaryRow / CleaningResult.java
├── src/test/java/...             JUnit 測試(日期、金額、清洗流程)
├── sample/sales_messy.csv        虛構雜亂範例資料
├── sample/output.xlsx            範例輸出
└── scripts/generate_sample.py    產生範例資料
```

## 限制 Notes
- `10/05/2026` 這類日期一律視為「月/日/年」;若資料是「日/月/年」需調整 `DateParser`。
  Slash dates with the year last are treated as MM/DD/YYYY.
- CSV 需為 UTF-8 編碼(可含 BOM);Big5 檔案請先轉檔。 Input must be UTF-8.
- 不支援欄位內換行的 CSV。 Multi-line quoted fields are not supported.

## License
MIT
