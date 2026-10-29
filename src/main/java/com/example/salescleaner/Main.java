package com.example.salescleaner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Sales CSV Cleaner 命令列入口。
 * 用法:java -jar target/sales-csv-cleaner.jar input.csv output.xlsx
 */
public final class Main {

    private static final int MAX_INVALID_SHOWN = 20;

    private Main() {
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }

    /** 回傳結束代碼:0 成功、1 參數錯誤、2 輸入檔問題、3 輸出失敗。 */
    static int run(String[] args) {
        if (args.length != 2) {
            System.err.println("用法:java -jar target/sales-csv-cleaner.jar <輸入.csv> <輸出.xlsx>");
            System.err.println("範例:java -jar target/sales-csv-cleaner.jar sample/sales_messy.csv sample/output.xlsx");
            return 1;
        }
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);

        if (!Files.exists(input)) {
            System.err.println("錯誤:找不到輸入檔案「" + input + "」,請確認路徑是否正確。");
            return 2;
        }
        if (!Files.isRegularFile(input) || !Files.isReadable(input)) {
            System.err.println("錯誤:無法讀取「" + input + "」,它不是一般檔案或沒有讀取權限。");
            return 2;
        }
        if (!output.toString().toLowerCase().endsWith(".xlsx")) {
            System.err.println("錯誤:輸出檔名必須以 .xlsx 結尾,例如 output.xlsx。");
            return 1;
        }

        CleaningResult result;
        try {
            List<List<String>> rows = CsvReader.read(input);
            result = new SalesCleaner().clean(rows);
        } catch (SalesCleaner.MissingColumnException e) {
            System.err.println("錯誤:" + e.getMessage());
            return 2;
        } catch (IOException e) {
            System.err.println("錯誤:讀取 CSV 失敗(請確認檔案為 UTF-8 編碼):" + e.getMessage());
            return 2;
        }

        List<SummaryRow> summary = Aggregator.summarize(result.records());
        try {
            ExcelWriter.write(output, result.records(), summary);
        } catch (IOException e) {
            System.err.println("錯誤:無法寫入「" + output + "」(檔案是否正在 Excel 中開啟?):" + e.getMessage());
            return 3;
        }

        printStats(result, summary.size(), output);
        return 0;
    }

    private static void printStats(CleaningResult r, int summaryRows, Path output) {
        System.out.println("=== Sales CSV Cleaner 處理統計 ===");
        System.out.println("讀入資料列:" + r.rowsRead() + " 列(不含表頭)");
        System.out.println("去除重複列:" + r.duplicatesRemoved() + " 列");
        System.out.println("無效列(已略過):" + r.invalidCount() + " 列");
        System.out.println("有效明細:" + r.records().size() + " 列");
        System.out.println("彙總(商品×月份):" + summaryRows + " 列");
        if (!r.invalidRows().isEmpty()) {
            System.out.println("--- 無效列明細 ---");
            r.invalidRows().stream().limit(MAX_INVALID_SHOWN).forEach(s -> System.out.println("  " + s));
            if (r.invalidCount() > MAX_INVALID_SHOWN) {
                System.out.println("  ……另有 " + (r.invalidCount() - MAX_INVALID_SHOWN) + " 列未顯示");
            }
        }
        System.out.println("已輸出:" + output.toAbsolutePath());
    }
}
