package com.example.salescleaner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** 負責檢查表頭、去除重複列、驗證並轉換每一列資料。 */
public final class SalesCleaner {

    /** 必要欄位及可接受的別名(中英文皆可)。 */
    static final Map<String, List<String>> REQUIRED_COLUMNS = Map.of(
            "訂單日期", List.of("訂單日期", "日期", "order date", "date"),
            "商品", List.of("商品", "商品名稱", "品名", "product"),
            "數量", List.of("數量", "qty", "quantity"),
            "單價", List.of("單價", "unit price", "price"));

    private static final List<String> COLUMN_ORDER = List.of("訂單日期", "商品", "數量", "單價");

    /** 表頭缺少必要欄位時丟出,訊息為中文。 */
    public static class MissingColumnException extends Exception {
        public MissingColumnException(String message) {
            super(message);
        }
    }

    public CleaningResult clean(List<List<String>> rows) throws MissingColumnException {
        if (rows.isEmpty()) {
            throw new MissingColumnException("CSV 檔案是空的,找不到表頭列。");
        }
        int[] idx = findColumns(rows.get(0));

        List<SalesRecord> records = new ArrayList<>();
        List<String> invalid = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int duplicates = 0;
        int dataRows = rows.size() - 1;

        for (int i = 1; i < rows.size(); i++) {
            int lineNo = i + 1; // 檔案中的實際行號
            List<String> row = rows.get(i);

            // 1) 完全空白的列
            if (row.stream().allMatch(f -> f.isBlank())) {
                invalid.add("第 " + lineNo + " 行:空白列");
                continue;
            }
            // 2) 完全重複的列(去掉前後空白後內容一模一樣)
            String key = String.join("\u0001", row.stream().map(String::trim).toList());
            if (!seen.add(key)) {
                duplicates++;
                continue;
            }
            // 3) 逐欄驗證
            String dateRaw = get(row, idx[0]);
            String product = get(row, idx[1]).trim();
            String qtyRaw = get(row, idx[2]);
            String priceRaw = get(row, idx[3]);

            List<String> problems = new ArrayList<>();
            Optional<LocalDate> date = DateParser.parse(dateRaw);
            if (date.isEmpty()) {
                problems.add(dateRaw.isBlank() ? "日期空白" : "日期格式無法辨識「" + dateRaw.trim() + "」");
            }
            if (product.isEmpty()) {
                problems.add("商品空白");
            }
            Optional<Integer> qty = AmountParser.parseQuantity(qtyRaw);
            if (qty.isEmpty()) {
                problems.add(qtyRaw.isBlank() ? "數量空白" : "數量無效「" + qtyRaw.trim() + "」");
            }
            Optional<BigDecimal> price = AmountParser.parseAmount(priceRaw);
            if (price.isEmpty()) {
                problems.add(priceRaw.isBlank() ? "單價空白" : "單價無效「" + priceRaw.trim() + "」");
            }

            if (problems.isEmpty()) {
                records.add(new SalesRecord(lineNo, date.get(), product, qty.get(), price.get()));
            } else {
                invalid.add("第 " + lineNo + " 行:" + String.join("、", problems));
            }
        }
        return new CleaningResult(records, dataRows, duplicates, invalid);
    }

    /** 找出必要欄位的位置,缺少時列出缺哪些欄位。 */
    private int[] findColumns(List<String> header) throws MissingColumnException {
        int[] idx = new int[COLUMN_ORDER.size()];
        List<String> missing = new ArrayList<>();
        for (int c = 0; c < COLUMN_ORDER.size(); c++) {
            String name = COLUMN_ORDER.get(c);
            idx[c] = -1;
            for (int h = 0; h < header.size(); h++) {
                String col = header.get(h).trim().toLowerCase(Locale.ROOT);
                if (REQUIRED_COLUMNS.get(name).contains(col)) {
                    idx[c] = h;
                    break;
                }
            }
            if (idx[c] < 0) {
                missing.add(name);
            }
        }
        if (!missing.isEmpty()) {
            throw new MissingColumnException("CSV 缺少必要欄位:" + String.join("、", missing)
                    + "。\n目前的表頭是:" + String.join(", ", header)
                    + "\n必要欄位:訂單日期、商品、數量、單價(也接受英文 Date, Product, Qty, Unit Price)。");
        }
        return idx;
    }

    private static String get(List<String> row, int i) {
        return i < row.size() ? row.get(i) : "";
    }
}
