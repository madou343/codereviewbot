// src/main/java/org/com/jambit/codereviewbot/summary/Aggregator.java
package org.com.jambit.codereviewbot.summary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.com.jambit.codereviewbot.client.JptClient;

import java.util.List;
import java.util.stream.Collectors;

public class Aggregator {

    private static final String MODEL = "code-review-summary";

    private final JptClient jpt = new JptClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private static int counter = 0;

    /**
     * Nimmt eine Liste von Strings, fügt sie zusammen und gibt die vom Modell
     * erzeugte zusammengefasste Antwort zurück (ein einzelner API-Call).
     */
    public String aggregate(List<String> rawResponses) {
        if (rawResponses == null || rawResponses.isEmpty()) {
            return "";
        }

        // Inhalte schlicht zusammenführen
        String prompt = rawResponses.stream()
                .map(s -> "- " + s)
                .collect(Collectors.joining("\n"));

        try {
            // OpenAI-kompatiblen Body bauen
            ObjectNode body = mapper.createObjectNode();
            body.put("model", MODEL);

            ArrayNode messages = mapper.createArrayNode();
            ObjectNode userMsg = mapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);
            body.set("messages", messages);

            System.out.println("➡️ Sending request body:\n" +
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));



            String response = jpt.sendRequest(mapper.writeValueAsString(body));
            return jpt.extractCompletionContent(response);

        } catch (Exception e) {
            throw new RuntimeException("Aggregation fehlgeschlagen", e);
        }
    }

    public static void writeSummaryToFile(String content, String filename) {
        if (content == null || content.isBlank()) {
            System.out.println("Kein Inhalt zum Schreiben vorhanden – Datei wird nicht erstellt.");
            return;
        }

        try {
            java.nio.file.Path outputPath = java.nio.file.Paths.get(filename);
            java.nio.file.Files.createDirectories(outputPath.getParent());

            // 🔹 Zähler hochzählen
            counter++;

            // 🔹 Text vorbereiten: Nummer + Inhalt + Leerzeile
            String numberedContent = "### Ausgabe " + counter + System.lineSeparator()
                    + content + System.lineSeparator() + System.lineSeparator();

            // 🔹 Datei anhängen statt überschreiben
            java.nio.file.Files.writeString(
                    outputPath,
                    numberedContent,
                    java.nio.charset.StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );

            System.out.println("✅ Ausgabe " + counter + " gespeichert unter: " + outputPath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ Fehler beim Schreiben der Datei: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
