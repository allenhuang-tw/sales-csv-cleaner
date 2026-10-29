package com.example.salescleaner;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** 用 Apache POI 輸出「清洗後明細」與「彙總」兩個工作表。 */
public final class ExcelWriter {

    public static final String DETAIL_SHEET = "清洗後明細";
    public static final String SUMMARY_SHEET = "彙總";

    private ExcelWriter() {
    }

    public static void write(Path output, List<SalesRecord> records, List<SummaryRow> summary) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Styles st = new Styles(wb);

            // ---- 工作表 1:清洗後明細 ----
            Sheet detail = wb.createSheet(DETAIL_SHEET);
            String[] dHead = {"原始行號", "訂單日期", "月份", "商品", "數量", "單價", "金額"};
            header(detail, dHead, st.header);
            int r = 1;
            for (SalesRecord rec : records) {
                Row row = detail.createRow(r++);
                num(row, 0, rec.sourceLine(), st.integer);
                Cell d = row.createCell(1);
                d.setCellValue(rec.orderDate());
                d.setCellStyle(st.date);
                row.createCell(2).setCellValue(rec.month().toString());
                row.createCell(3).setCellValue(rec.product());
                num(row, 4, rec.quantity(), st.integer);
                money(row, 5, rec.unitPrice(), st.money);
                money(row, 6, rec.amount(), st.money);
            }
            detail.createFreezePane(0, 1);
            autoWidth(detail, dHead.length);

            // ---- 工作表 2:彙總(商品 × 月份)----
            Sheet sum = wb.createSheet(SUMMARY_SHEET);
            String[] sHead = {"商品", "月份", "總數量", "銷售總額"};
            header(sum, sHead, st.header);
            r = 1;
            long totalQty = 0;
            BigDecimal totalAmt = BigDecimal.ZERO;
            for (SummaryRow s : summary) {
                Row row = sum.createRow(r++);
                row.createCell(0).setCellValue(s.product());
                row.createCell(1).setCellValue(s.month().toString());
                num(row, 2, s.totalQuantity(), st.integer);
                money(row, 3, s.totalAmount(), st.money);
                totalQty += s.totalQuantity();
                totalAmt = totalAmt.add(s.totalAmount());
            }
            Row total = sum.createRow(r);
            Cell label = total.createCell(0);
            label.setCellValue("合計");
            label.setCellStyle(st.header);
            num(total, 2, totalQty, st.integerBold);
            money(total, 3, totalAmt, st.moneyBold);
            sum.createFreezePane(0, 1);
            autoWidth(sum, sHead.length);

            Path parent = output.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream os = Files.newOutputStream(output)) {
                wb.write(os);
            }
        }
    }

    private static void header(Sheet sheet, String[] names, CellStyle style) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < names.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(names[i]);
            c.setCellStyle(style);
        }
    }

    private static void num(Row row, int col, long v, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(v);
        c.setCellStyle(style);
    }

    private static void money(Row row, int col, BigDecimal v, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(v.doubleValue());
        c.setCellStyle(style);
    }

    /**
     * 依內容自動調整欄寬。POI 內建的 autoSizeColumn 需要系統字型,
     * 在沒有字型的伺服器上常會失敗或把中文算太窄,所以這裡自己估算:
     * 中文等全形字算 2 個字元寬,其他算 1 個。
     */
    private static void autoWidth(Sheet sheet, int cols) {
        int[] width = new int[cols];
        for (Row row : sheet) {
            for (int c = 0; c < cols; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) {
                    continue;
                }
                String text = switch (cell.getCellType()) {
                    case NUMERIC -> String.format("%,.2f", cell.getNumericCellValue());
                    default -> cell.toString();
                };
                width[c] = Math.max(width[c], displayWidth(text));
            }
        }
        for (int c = 0; c < cols; c++) {
            sheet.setColumnWidth(c, Math.min(255, width[c] + 3) * 256);
        }
    }

    static int displayWidth(String s) {
        int w = 0;
        for (char ch : s.toCharArray()) {
            w += (ch >= 0x2E80) ? 2 : 1;
        }
        return w;
    }

    /** 集中管理儲存格樣式:粗體表頭、日期、整數與千分位金額。 */
    private static final class Styles {
        final CellStyle header;
        final CellStyle date;
        final CellStyle integer;
        final CellStyle integerBold;
        final CellStyle money;
        final CellStyle moneyBold;

        Styles(Workbook wb) {
            Font bold = wb.createFont();
            bold.setBold(true);
            short intFmt = wb.createDataFormat().getFormat("#,##0");

            header = wb.createCellStyle();
            header.setFont(bold);

            date = wb.createCellStyle();
            date.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd"));

            integer = wb.createCellStyle();
            integer.setDataFormat(intFmt);
            integerBold = wb.createCellStyle();
            integerBold.setDataFormat(intFmt);
            integerBold.setFont(bold);

            money = wb.createCellStyle();
            money.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
            moneyBold = wb.createCellStyle();
            moneyBold.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
            moneyBold.setFont(bold);
        }
    }
}
