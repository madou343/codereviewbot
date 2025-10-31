package org.com.jambit.codereviewbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.com.jambit.codereviewbot.client.JptClient;
import org.com.jambit.codereviewbot.summary.Aggregator;
import org.com.jambit.codereviewbot.util.FileCollector;
import org.com.jambit.codereviewbot.util.PromtPacker;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static String testGithubUrl = "https://github.com/madou343/websiteChecker.git";
    //static String testFilePath = "C:\\Users\\mtheele\\CodeReviewBot\\ClonedRepos";
    static String testFilePath = "C:\\Users\\mtheele\\IdeaProjects\\TIP - Assortmenttool_2";

    public static void main(String[] args) throws Exception {
        System.out.println("Willkommen beim CodeReviewBot");

        FileCollector collector = new FileCollector();

        List<File> importantFiles = collector.collectFiles(testFilePath);

        List<String> promtsArray = PromtPacker.buildCodeReviewChunks(importantFiles);
        ObjectMapper mapper = new ObjectMapper();

        List<String> responses = new ArrayList<String>();

        int zahl = 0;

        for (int i = 0; i < Math.min(20, promtsArray.size()); i++) {
            String promt = promtsArray.get(i);
            zahl++;
            System.out.println("Durchlauf " + zahl + " von " + promtsArray.size());
            int remaining = promtsArray.size() - zahl;
            int secondsLeft = remaining * 110;
            System.out.printf("⏳  ≈ %d Sek. (%.1f Min) verbleibend%n", secondsLeft, secondsLeft / 60.0);

            JsonNode promtNode = mapper.readTree(promt);

            // neuen Request-Body bauen
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("model", "codereviewbot20");
            requestBody.set("messages", mapper.createArrayNode().add(promtNode));

            String body = mapper.writeValueAsString(requestBody);

            JptClient api = new JptClient();

            String response = api.sendRequest(body);

            System.out.println("Das ist der " + zahl + "durchlauf");
            System.out.println(response);
            responses.add(api.extractCompletionContent(response));

        }

        Aggregator aggregator = new Aggregator();

        String finalResponse = aggregator.aggregate(responses);
        Aggregator.writeSummaryToFile(finalResponse, "src/main/resources/review-summary.txt");

    }
}