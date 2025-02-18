package com.example.documentintelligence.infrastructure.adapter;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class JsonPathProcessor {

    private static final String TOKEN_REGEX = "\\$\\{[^#]+}";


    public static final Configuration config = Configuration.builder()
                                                            .options(Option.ALWAYS_RETURN_LIST)
                                                            .build();

    static {
        config.addOptions(Option.ALWAYS_RETURN_LIST);
    }

    public static List<String> replaceTokens(String jsonPath, String content, Map<String, String> pendingPaths) {
        return replacePendingTokens(jsonPath, content, pendingPaths);
    }

    private static List<String> replacePendingTokens(String jsonPath, String content, Map<String, String> pendingJsonPaths) {
        log.info("Will try to replace tokens from: {}", jsonPath);
        Map<String, List<String>> expandedJsonPaths = new LinkedHashMap<>();
        JsonPathProcessor processor = new JsonPathProcessor();
        Map<String, List<String>> resolvedTokens = processor.expandJsonPathsRecursively(pendingJsonPaths, content, expandedJsonPaths);

        return generateJsonPathCombinations(jsonPath, resolvedTokens);
    }

    private static List<String> generateJsonPathCombinations(String template, Map<String, List<String>> tokenMappings) {
        List<String> results = new ArrayList<>(Collections.singletonList(template));

        for (Map.Entry<String, List<String>> entry : tokenMappings.entrySet()) {
            List<String> tempResults = new ArrayList<>();
            for (String partial : results) {
                for (String value : entry.getValue()) {
                    tempResults.add(partial.replace(entry.getKey(), value));
                }
            }
            results = tempResults;
        }
        return results;
    }

    private Map<String, List<String>> expandJsonPathsRecursively(Map<String, String> pendingPaths, String documentData, Map<String, List<String>> expandedPaths) {
        log.info("Recursive call input: pendingPaths={}, expandedPaths={}", pendingPaths, expandedPaths);
        Map<String, String> unresolvedPaths = new LinkedHashMap<>(pendingPaths);

        pendingPaths.forEach((key, value) -> {
            List<String> extractedValues = extractValuesFromJson(value, documentData, expandedPaths);
            if (!extractedValues.isEmpty()) {
                expandedPaths.put(key, extractedValues);
                unresolvedPaths.remove(key);
            }
        });

        if (unresolvedPaths.isEmpty()) {
            log.info("Recursive call result: {}", expandedPaths);
            return expandedPaths;
        }
        return expandJsonPathsRecursively(unresolvedPaths, documentData, expandedPaths);
    }

    private List<String> extractValuesFromJson(String jsonPath, String documentData, Map<String, List<String>> expandedPaths) {
        List<String> resolvedPaths = resolveTokensInPath(jsonPath, expandedPaths);
        List<String> extractedValues = new LinkedList<>();

        resolvedPaths.forEach(path -> {
            try {
                List<?> extractedData = JsonPath.using(config).parse(documentData).read(path);
                extractedData.forEach(value -> extractedValues.add(String.valueOf(value)));
            } catch (com.jayway.jsonpath.JsonPathException e) {
                log.warn("Invalid JSONPath expression: " + path);
            }
        });
        return extractedValues;
    }

    private List<String> resolveTokensInPath(String path, Map<String, List<String>> tokenMappings) {
        List<String> resolvedPaths = new LinkedList<>();
        Matcher matcher = Pattern.compile(TOKEN_REGEX).matcher(path);

        while (matcher.find()) {
            String token = matcher.group(0);
            List<String> values = tokenMappings.getOrDefault(token, Collections.emptyList());
            values.forEach(value -> resolvedPaths.add(path.replace(token, value)));
        }

        return resolvedPaths.isEmpty() ? Collections.singletonList(path) : resolvedPaths;
    }
}
