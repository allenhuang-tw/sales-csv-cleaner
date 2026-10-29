package com.example.salescleaner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class SalesCleanerTest {

    @Test
    void removesDuplicatesSkipsInvalidAndSummarizes() throws Exception {
        List<List<String>> rows = List.of(
                List.of("訂單日期", "商品", "數量", "單價"),
                List.of("2026/10/05", "珍珠奶茶", "2", "$55"),
                List.of("2026/10/05", "珍珠奶茶", "2", "$55"),   // 重複
                List.of("10/20/2026", "珍珠奶茶", "1", "NT$55"),
                List.of("", "", "", ""),                          // 空白列
                List.of("2026-11-01", "", "3", "100"));           // 商品空白
        CleaningResult r = new SalesCleaner().clean(rows);
        assertEquals(5, r.rowsRead());
        assertEquals(1, r.duplicatesRemoved());
        assertEquals(2, r.invalidCount());
        assertEquals(2, r.records().size());

        List<SummaryRow> s = Aggregator.summarize(r.records());
        assertEquals(1, s.size());
        assertEquals(3, s.get(0).totalQuantity());
        assertEquals(0, new BigDecimal("165").compareTo(s.get(0).totalAmount()));
    }

    @Test
    void reportsMissingColumnsInChinese() {
        List<List<String>> rows = List.of(List.of("日期", "商品"));
        var e = assertThrows(SalesCleaner.MissingColumnException.class, () -> new SalesCleaner().clean(rows));
        assertTrue(e.getMessage().contains("數量"));
        assertTrue(e.getMessage().contains("單價"));
    }
}
