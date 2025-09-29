package org.com.jambit.codereviewbot.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.com.jambit.codereviewbot.Main;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class JptClient {

    private static final String API_URL = "https://jpt.jambit.io/api/chat/completions";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String sendRequest(String body) throws Exception {

        Properties props = new Properties();
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                throw new FileNotFoundException("application.properties nicht gefunden!");
            }
        }

        String apiKey = props.getProperty("jpt.api.key");


        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + res.statusCode());

        return res.body();
    }

    public void printCompletionContent(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);
            JsonNode choices = root.path("choices");

            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText();
                System.out.println("=== Review-Ausgabe ===\n");
                for (String line : content.split("\n")) {
                    System.out.println(line.trim());
                }
                System.out.println("\n======================");
            } else {
                System.out.println("⚠️ Keine 'choices' im Response gefunden.");
            }
        } catch (Exception e) {
            System.err.println("❌ Fehler beim Parsen: " + e.getMessage());
            System.out.println(responseBody);
        }
    }
}
