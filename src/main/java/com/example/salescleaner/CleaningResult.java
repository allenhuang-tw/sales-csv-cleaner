package com.example.salescleaner;

import java.util.List;

/** 清洗結果與處理統計。 */
public record CleaningResult(
        List<SalesRecord> records,
        int rowsRead,
        int duplicatesRemoved,
        List<String> invalidRows) {

    public int invalidCount() {
        return invalidRows.size();
    }
}
