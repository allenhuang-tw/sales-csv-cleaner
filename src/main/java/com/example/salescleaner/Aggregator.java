package com.example.salescleaner;

import java.math.BigDecimal;
import java.text.Collator;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 依「商品 × 月份」彙總銷售數量與金額。 */
public final class Aggregator {

    private Aggregator() {
    }

    public static List<SummaryRow> summarize(List<SalesRecord> records) {
        record Key(String product, YearMonth month) {
        }
        Map<Key, int[]> qty = new LinkedHashMap<>();
        Map<Key, BigDecimal> amt = new LinkedHashMap<>();
        for (SalesRecord r : records) {
            Key k = new Key(r.product(), r.month());
            qty.computeIfAbsent(k, x -> new int[1])[0] += r.quantity();
            amt.merge(k, r.amount(), BigDecimal::add);
        }
        List<SummaryRow> out = new ArrayList<>();
        qty.forEach((k, q) -> out.add(new SummaryRow(k.product(), k.month(), q[0], amt.get(k))));

        // 商品依中文排序,同商品再依月份排序
        Collator zh = Collator.getInstance(Locale.TRADITIONAL_CHINESE);
        out.sort(Comparator.comparing(SummaryRow::product, zh).thenComparing(SummaryRow::month));
        return out;
    }
}
