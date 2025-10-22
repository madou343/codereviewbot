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
    private static final int MAX_OUTPUT_TOKENS = 2000;

    private final JptClient jpt = new JptClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Nimmt eine Liste von Strings, fügt sie zusammen und gibt die vom Modell
     * erzeugte zusammengefasste Antwort zurück (ein einzelner API-Call).
     */
    public String aggregate(List<String> rawResponses) {
        if (rawResponses == null || rawResponses.isEmpty()) {
            return "";
        }

        // Inhalte schlicht zusammenführen
        String merged = rawResponses.stream()
                .map(s -> "- " + s)
                .collect(Collectors.joining("\n"));

        // Minimaler Prompt (ohne Templates)
        String prompt = "Fasse die folgenden Punkte prägnant und ohne Dopplungen zusammen:\n\n"
                + merged
                + "\n\nAntworte kurz und strukturiert.";

        try {
            // OpenAI-kompatiblen Body bauen
            ObjectNode body = mapper.createObjectNode();
            body.put("model", MODEL);
            body.put("max_tokens", MAX_OUTPUT_TOKENS);

            ArrayNode messages = mapper.createArrayNode();
            ObjectNode userMsg = mapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);
            body.set("messages", messages);

            String response = jpt.sendRequest(mapper.writeValueAsString(body));
            return jpt.extractCompletionContent(response);

        } catch (Exception e) {
            throw new RuntimeException("Aggregation fehlgeschlagen", e);
        }
    }
}
