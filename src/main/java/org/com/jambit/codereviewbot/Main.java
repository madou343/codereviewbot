package org.com.jambit.codereviewbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.com.jambit.codereviewbot.client.JptClient;
import org.com.jambit.codereviewbot.git.RepoFetcher;
import org.com.jambit.codereviewbot.summary.Aggregator;
import org.com.jambit.codereviewbot.util.FileCollector;
import org.com.jambit.codereviewbot.util.PromtPacker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

        for (String promt : promtsArray) {
            zahl++;
            System.out.println("Durchlauf " + zahl + " von " + promtsArray.size());
            int remaining = promtsArray.size() - zahl;
            int secondsLeft = remaining * 110;
            System.out.printf("⏳  ≈ %d Sek. (%.1f Min) verbleibend%n", secondsLeft, secondsLeft / 60.0);

            JsonNode promtNode = mapper.readTree(promt);

            // neuen Request-Body bauen
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("model", "codereviewbot");
            requestBody.set("messages", mapper.createArrayNode().add(promtNode));

            String body = mapper.writeValueAsString(requestBody);

            JptClient api = new JptClient();

            String response = api.sendRequest(body);

            responses.add(api.extractCompletionContent(response));

        }

        Aggregator aggregator = new Aggregator();

        String finalResponse = aggregator.aggregate(responses);
        System.out.println(finalResponse);

    }
}