package com.example.salescleaner;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** 簡易 CSV 讀取器:支援雙引號包住的欄位(例如 "$1,200")與 UTF-8 BOM。 */
public final class CsvReader {

    private CsvReader() {
    }

    /** 讀取整個檔案,每個元素是一列(原始行號從 1 開始,等於索引 + 1)。 */
    public static List<List<String>> read(Path file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first && line.startsWith("\uFEFF")) {
                    line = line.substring(1); // 去掉 Excel 存檔時常見的 BOM
                }
                first = false;
                rows.add(parseLine(line));
            }
        }
        return rows;
    }

    static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"'); // "" 代表一個雙引號
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        fields.add(cur.toString());
        return fields;
    }
}
