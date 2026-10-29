package com.example.salescleaner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Optional;

/**
 * 解析混合格式的日期字串。
 * 支援:2026/10/05、2026-10-05、2026.10.05、20261005、10/05/2026(視為 月/日/年)。
 * 月、日可為一位數(例如 2026/1/5)。
 */
public final class DateParser {

    private static final List<DateTimeFormatter> FORMATS = List.of(
            strict("uuuu/M/d"),
            strict("uuuu-M-d"),
            strict("uuuu.M.d"),
            strict("uuuuMMdd"),
            strict("M/d/uuuu"),
            strict("M-d-uuuu"));

    private DateParser() {
    }

    private static DateTimeFormatter strict(String pattern) {
        // STRICT 會拒絕 2026/02/30 這類不存在的日期
        return DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    }

    /** 解析成功回傳日期,失敗(空白或格式不符)回傳 Optional.empty()。 */
    public static Optional<LocalDate> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return Optional.empty();
        }
        for (DateTimeFormatter f : FORMATS) {
            try {
                return Optional.of(LocalDate.parse(s, f));
            } catch (DateTimeParseException ignored) {
                // 換下一種格式
            }
        }
        return Optional.empty();
    }
}
