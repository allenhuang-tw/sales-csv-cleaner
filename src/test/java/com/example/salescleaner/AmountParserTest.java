package com.example.salescleaner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class AmountParserTest {

    @Test
    void stripsCurrencySymbolsAndThousandSeparators() {
        assertEquals(new BigDecimal("1250"), AmountParser.parseAmount("NT$1,250").orElseThrow());
        assertEquals(new BigDecimal("3000.50"), AmountParser.parseAmount("$ 3,000.50").orElseThrow());
        assertEquals(new BigDecimal("12800"), AmountParser.parseAmount("12,800").orElseThrow());
        assertEquals(new BigDecimal("980"), AmountParser.parseAmount("980元").orElseThrow());
    }

    @Test
    void rejectsInvalidAmountsAndQuantities() {
        assertTrue(AmountParser.parseAmount("").isEmpty());
        assertTrue(AmountParser.parseAmount("abc").isEmpty());
        assertTrue(AmountParser.parseAmount("-100").isEmpty());
        assertTrue(AmountParser.parseQuantity("0").isEmpty());
        assertTrue(AmountParser.parseQuantity("2.5").isEmpty());
        assertEquals(1200, AmountParser.parseQuantity("1,200").orElseThrow());
    }
}
