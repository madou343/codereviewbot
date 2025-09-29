package org.com.jambit.codereviewbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.com.jambit.codereviewbot.client.JptClient;
import org.com.jambit.codereviewbot.git.RepoFetcher;
import org.com.jambit.codereviewbot.util.FileCollector;
import org.com.jambit.codereviewbot.util.PromtPacker;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class Main {

    static String testGithubUrl = "https://github.com/madou343/websiteChecker.git";
    static String testFilePath = "C:\\Users\\mtheele\\CodeReviewBot\\ClonedRepos";

    public static void main(String[] args) throws Exception {
        System.out.println("Willkommen beim CodeReviewBot");
        System.out.println("Bitte nenne mir die Github URl vom Repository?:");

        Scanner scanner = new Scanner(System.in);
        String githubUrl = scanner.nextLine();

        System.out.println("Bitte nenne mir den Filepath wo hin kopiert werden soll");
        String filepath = scanner.nextLine();


        RepoFetcher fetcher = new RepoFetcher();
        fetcher.fetchRepo(testGithubUrl, testFilePath);
         FileCollector collector = new FileCollector();

        List<File> importantFiles = collector.collectFiles(testFilePath);

        List<String> promtsArray = PromtPacker.buildCodeReviewChunks(importantFiles);
        ObjectMapper mapper = new ObjectMapper();

        for (String promt : promtsArray) {

            JsonNode promtNode = mapper.readTree(promt);

            // neuen Request-Body bauen
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("model", "codereviewbot");
            requestBody.set("messages", mapper.createArrayNode().add(promtNode));

            String body = mapper.writeValueAsString(requestBody);

            JptClient api = new JptClient();

            String response = api.sendRequest(body);
            api.printCompletionContent(response);


        }

    }
}