package org.com.jambit.codereviewbot.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileCollector {

    public static List<File> collectFiles(String repoPath) throws IOException {

        List<String> allowedExtensions = Arrays.asList(".java", ".xml", ".md");

        try (var stream = Files.walk(Paths.get(repoPath))) {
            List<File> collect = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return allowedExtensions.stream().anyMatch(name::endsWith);
                    })
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            return collect;
        }
    }
}
