package org.com.jambit.codereviewbot.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JptClientTest {

    @Test
    void testJptClient()throws Exception {
        String body = """
            {
              "model": "codereviewbot",
              "messages": [
                {"role": "user", "content": "Sag Hallo in einem Satz"}
              ]
            }
            """;

        JptClient client = new JptClient(); // deine Klasse mit sendRequest()

        String response = client.sendRequest(body);

        System.out.println("Antwort:\n" + response);

        // ganz einfacher Check: Response darf nicht leer sein
        assertNotNull(response, "Response darf nicht null sein");
        assertTrue(response.contains("choices"), "Response sollte 'choices' enthalten");
    }

}