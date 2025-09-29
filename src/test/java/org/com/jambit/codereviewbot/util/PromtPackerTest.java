package org.com.jambit.codereviewbot.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromtPackerTest {

    @TempDir
    Path tmp;

    @Test
    void readFileSafe_readsUtf8() throws IOException {
        Path f = tmp.resolve("a.txt");
        Files.writeString(f, "hello", StandardCharsets.UTF_8);

        // je nachdem, wo die Methode liegt:
        String s = PromtPacker.readFileSafe(f.toFile()); // oder FileUtil.readFileSafe(f.toFile())
        assertNotNull(s);
        assertTrue(s.contains("hello"));

}

@Test
    void buildCodeReviewChunkTest() throws IOException {
    List<File> testListe = new ArrayList<>();

        File f1 = new File("example1.txt");
        Files.writeString(f1.toPath(), "Dies ist der Inhalt der ersten Beispieldatei", StandardCharsets.UTF_8);

        File f2 = new File("example2.txt");
        Files.writeString(f2.toPath(), "Hier steht etwas anderes in der zweiten Datei.", StandardCharsets.UTF_8);

    Collections.addAll(testListe, f1, f2);

        List<String> promtString = PromtPacker.buildCodeReviewChunks(testListe);

    assertTrue(!promtString.isEmpty(), "Es sollte mindestens ein Chunk erzeugt werden");
    assertTrue(promtString.get(0).contains("Dies ist der Inhalt der ersten Beispieldatei"),
            "Der Inhalt von f1 sollte im ersten Chunk stehen");
    assertTrue(promtString.get(0).contains("Hier steht etwas anderes in der zweiten Datei."),
            "Der Inhalt von f2 sollte im ersten Chunk stehen");
}
}