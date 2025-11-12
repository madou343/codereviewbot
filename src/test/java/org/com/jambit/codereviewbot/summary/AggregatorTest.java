package org.com.jambit.codereviewbot.summary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

class AggregatorTest {

    // Simulierter Output von einem einzelnen Chunk (das, was du normalerweise vom Modell bekommst)
    private static final String reviewJson1 = """
        {
          "file": "HomeController.java",
          "issues": [
            {
              "severity": "High",
              "problem": "Potential SSRF / open-network scan – user-controlled `url` is fetched without validation or allow-list.",
              "justification": "Angreifer könnten interne Services scannen oder ansprechen.",
              "action": "Validate that `url` is absolute HTTP/HTTPS and matches a whitelist; reject everything else with HTTP 400.",
              "source": "search() line 31-39",
              "component": "controller/security",
              "impact_scope": "prod-kritisch",
              "relations": null,
              "evidence": null
            },
            {
              "severity": "Med",
              "problem": "No connection timeout on HttpURLConnection.",
              "justification": "Kann zu Hängern/DoS führen.",
              "action": "Set connect/read timeouts (5s); add test simulating timeout and expecting 504.",
              "source": "search() line 44-48",
              "component": "controller/performance",
              "impact_scope": "prod-relevant",
              "relations": null,
              "evidence": null
            },
            {
              "severity": 'Low',
              "problem": "System.out/System.err statt Logger.",
              "justification": "Kein strukturiertes Logging, schwer zu aggregieren.",
              "action": "Ersetze Ausgaben durch SLF4J Logger und sinnvolle Log-Level.",
              "source": "search() line 23,57,68",
              "component": "observability",
              "impact_scope": "low",
              "relations": null,
              "evidence": null
            }
          ],
          "summary": {
            "overall_rating": "4",
            "top_todos": [
              "SSRF absichern (Whitelist + Validierung)",
              "Timeouts für externe Calls konfigurieren",
              "SLF4J Logging statt System.out verwenden"
            ]
          }
        }
        """;

    @Test
    void aggregatorTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 1. Parse den simulierten Modell-Output (so wie du ihn pro Chunk speichern würdest)
        JsonNode node1 = mapper.readTree(reviewJson1);

        // Falls du mehrere Chunks testen willst, könntest du hier node2, node3 usw. bauen
        List<JsonNode> inputListe = List.of(node1);

        // 2. Aggregator aufrufen
        Aggregator aggregator = new Aggregator();

        String output = aggregator.aggregate(inputListe);

        System.out.println("=== Aggregated Tickets ===");
        System.out.println(output);

        // 3. (Optional) Gültiges JSON erzwingen
        //    Erwartung: output sollte ein JSON sein wie { "tickets": [ ... ] }
        try {
            mapper.readTree(output); // wenn das scheitert, war die Antwort kein valides JSON
        } catch (Exception e) {
            throw new AssertionError("Aggregator output ist kein valides JSON: " + output, e);
        }
    }
}
