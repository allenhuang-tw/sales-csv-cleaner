package com.example.salescleaner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DateParserTest {

    @ParameterizedTest
    @ValueSource(strings = {"2026/10/05", "2026-10-05", "10/05/2026", "2026.10.05", "20261005", " 2026/10/5 "})
    void parsesMixedFormatsToSameDate(String raw) {
        assertEquals(LocalDate.of(2026, 10, 5), DateParser.parse(raw).orElseThrow());
    }

    @Test
    void rejectsBlankAndInvalidDates() {
        assertTrue(DateParser.parse("").isEmpty());
        assertTrue(DateParser.parse(null).isEmpty());
        assertTrue(DateParser.parse("2026/02/30").isEmpty()); // 不存在的日期
        assertTrue(DateParser.parse("下週一").isEmpty());
    }
}
