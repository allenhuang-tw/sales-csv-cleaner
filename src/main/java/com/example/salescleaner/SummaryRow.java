package com.example.salescleaner;

import java.math.BigDecimal;
import java.time.YearMonth;

/** 彙總表的一列:某商品在某月份的總數量與總金額。 */
public record SummaryRow(String product, YearMonth month, int totalQuantity, BigDecimal totalAmount) {
}
