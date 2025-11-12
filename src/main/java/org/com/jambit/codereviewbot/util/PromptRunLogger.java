package org.com.jambit.codereviewbot.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PromptRunLogger {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Path LOG_DIR = Path.of("logs");

    static {
        try {
            Files.createDirectories(LOG_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Konnte Log-Verzeichnis nicht erstellen", e);
        }
    }

    /**
     * Loggt Prompt und Response als JSON-Datei mit Metadaten.
     */
    public static void logPromptResponse(JsonNode promptJson, JsonNode responseJson, int index, String fileName) {
        try {
            ObjectNode logNode = mapper.createObjectNode();
            logNode.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            logNode.put("index", index);
            logNode.put("file", fileName);

            // Prompt und Response als echte JSON-Strukturen anhängen
            logNode.set("prompt", promptJson);
            logNode.set("response", responseJson);

            // optional: Laufzeit, Dauer, Status usw.
            // logNode.put("durationMs", 1200);

            String safeName = fileName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
            Path filePath = LOG_DIR.resolve(String.format("run_%03d_%s.json", index, safeName));

            mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), logNode);

            System.out.println("📝 JSON-Log gespeichert: " + filePath);
        } catch (IOException e) {
            System.err.println("❌ Fehler beim Schreiben des Logs: " + e.getMessage());
        }
    }

    /**
     * Loggt nur eine zusammengefasste Text- oder JSON-Antwort (z. B. den finalen Aggregator-Output),
     * ohne dass ein separater Prompt-JSON vorliegt.
     *
     * Ideal für Fälle, in denen du nur das Endergebnis oder einen einfachen String speichern willst.
     *
     * @param label   Kurzbeschreibung oder Dateiname (z. B. "final-aggregation" oder "ticket-summary")
     * @param content Der Text oder das JSON, das geloggt werden soll
     */
    public static void logSimpleResult(String label, String content) {
        try {
            ObjectNode logNode = mapper.createObjectNode();
            logNode.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            logNode.put("label", label);

            // Versuch: prüfen, ob content bereits valides JSON ist
            try {
                JsonNode parsed = mapper.readTree(content);
                logNode.set("result", parsed);
            } catch (Exception e) {
                // Wenn nicht parsebar → als einfacher Text speichern
                logNode.put("result_raw", content);
            }

            String safeName = label.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
            Path filePath = LOG_DIR.resolve(String.format("simple_%s_%s.json",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                    safeName));

            mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), logNode);

            System.out.println("📝 Einfaches Log gespeichert: " + filePath);
        } catch (IOException e) {
            System.err.println("❌ Fehler beim Schreiben des simplen Logs: " + e.getMessage());
        }
    }

    /**
     * Loggt eine formatierte, menschenlesbare Ausgabe (z. B. Markdown oder generierte Tickets)
     * in einer sauberen JSON-Struktur mit Einrückung.
     *
     * @param label   Log-Bezeichner (z. B. "final-aggregation" oder "ticket-cluster")
     * @param content Der vom Modell generierte Markdown- oder Text-Output
     */
    public static void logFormattedResult(String label, String content) {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            root.put("label", label);

            // Zerlege den Body in Zeilen und speichere als Array statt als \n-String
            String[] lines = content.split("\\R"); // \\R = any line break

            ObjectNode mdNode = mapper.createObjectNode();
            mdNode.put("type", "markdown/text");
            mdNode.put("length", content.length());
            mdNode.put("lines", lines.length);

            // kleine Vorschau
            String preview = lines.length > 0 ? lines[0] : "";
            if (preview.length() > 200) {
                preview = preview.substring(0, 200) + "...";
            }
            mdNode.put("preview_first_line", preview);

            // Das eigentliche Markdown Zeile für Zeile als Array
            var bodyArray = mapper.createArrayNode();
            for (String line : lines) {
                bodyArray.add(line);
            }
            mdNode.set("body_lines", bodyArray);

            root.set("result_markdown", mdNode);

            // Dateiname bauen
            String safeName = label.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path filePath = LOG_DIR.resolve(String.format("perfect_%s_%s.json", timestamp, safeName));

            mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), root);

            System.out.println("🪄 Nice-Markdown-Log gespeichert: " + filePath);

        } catch (IOException e) {
            System.err.println("❌ Fehler beim Schreiben des Pretty-Logs: " + e.getMessage());
        }
    }

}
