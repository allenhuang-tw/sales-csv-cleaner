package com.example.salescleaner;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 解析金額與數量字串:移除「NT$」「US$」「$」「元」、千分位逗號與空白。
 * 例如 "NT$1,250"、"$ 3,000.50"、"980元" 都能正確解析。負數視為無效。
 */
public final class AmountParser {

    private AmountParser() {
    }

    public static Optional<BigDecimal> parseAmount(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String s = raw.trim()
                .replaceFirst("(?i)^(NT|US)?\\$", "")
                .replace("元", "")
                .replace(",", "")
                .replace(",", "")   // 全形逗號
                .replace(" ", "")
                .trim();
        if (s.isEmpty() || !s.matches("\\d+(\\.\\d+)?")) {
            return Optional.empty();
        }
        return Optional.of(new BigDecimal(s));
    }

    /** 數量必須是正整數(允許千分位,如 "1,200")。 */
    public static Optional<Integer> parseQuantity(String raw) {
        return parseAmount(raw)
                .filter(v -> v.signum() > 0 && v.stripTrailingZeros().scale() <= 0)
                .map(v -> {
                    try {
                        return v.intValueExact();
                    } catch (ArithmeticException e) {
                        return null;
                    }
                });
    }
}
