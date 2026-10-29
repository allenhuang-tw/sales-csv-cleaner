package com.example.salescleaner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/** 一筆清洗後的銷售資料。金額 = 數量 × 單價。 */
public record SalesRecord(int sourceLine, LocalDate orderDate, String product, int quantity, BigDecimal unitPrice) {

    public BigDecimal amount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public YearMonth month() {
        return YearMonth.from(orderDate);
    }
}
