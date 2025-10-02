package org.com.jambit.codereviewbot.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PromtPacker {

    private static final int MAX_CHUNK_CHARS = 120_000;
    private static final int PER_FILE_SLICE_CHARS = 60_000;

    public static List<String> buildCodeReviewChunks(List<File> fileList) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (File p : fileList) {
            String fileText = readFileSafe(p);
            if (fileText == null) continue;

            // große Dateien stückeln
            List<String> slices = slice(fileText, PER_FILE_SLICE_CHARS);

            for (int i = 0; i < slices.size(); i++) {
                String sliceLabel = slices.size() > 1 ? " (Teil " + (i + 1) + "/" + slices.size() + ")" : "";

                String block = """
                ===== BEGIN FILE: %s%s =====
                ```
                %s
                ```
                ===== END FILE: %s%s =====

                """.formatted(
                        p.toString(), sliceLabel,
                        slices.get(i),
                        p.toString(), sliceLabel
                );

                // Wenn der nächste Block das Chunk-Limit sprengt → aktuellen Chunk flushen
                if (current.length() + block.length() > MAX_CHUNK_CHARS) {
                    ObjectNode msg = mapper.createObjectNode()
                            .put("role", "user")
                            .put("content", current.toString());

                    chunks.add(mapper.writeValueAsString(msg));

                    current.setLength(0);
                }

                current.append(block);
            }
        }

        // letzten (nicht-leeren) Chunk hinzufügen
        if (current.length() > 0) {
            ObjectNode msg = new ObjectMapper().createObjectNode()
                    .put("role", "user")
                    .put("content", current.toString());

            chunks.add(mapper.writeValueAsString(msg));
        }

        return chunks;
    }

    private static List<String> slice(String text, int maxChars) {
        if (text == null) return List.of();
        if (text.length() <= maxChars) return List.of(text);

        List<String> parts = new ArrayList<>();
        int i = 0;

        while (i < text.length()) {
            int end = Math.min(text.length(), i + maxChars);

            int nl = text.lastIndexOf('\n', end - 1);

            if (nl < i + (int)(maxChars * 0.7) || nl < 0) {
                nl = end;
            }

            if (nl <= i) nl = end;

            parts.add(text.substring(i, nl));
            i = nl;
        }

        return parts;
    }

    public static String readFileSafe(File p) {
        try {
            // Lies als UTF-8; für andere Encodings ggf. Detection ergänzen
            String content = Files.readString(p.toPath(), StandardCharsets.UTF_8);
            if (!looksTextual(content)) return null;
            return content;
        } catch (Exception e) {
            return "/* Fehler beim Lesen: " + e.getMessage() + " */";
        }
    }
    private static boolean looksTextual(String content) {
        long nonPrintable = content.chars().filter(ch -> ch != '\n' && ch != '\r' && ch != '\t' && (ch < 32 || ch == 127)).count();
        return nonPrintable < content.length() * 0.01;
    }
}
