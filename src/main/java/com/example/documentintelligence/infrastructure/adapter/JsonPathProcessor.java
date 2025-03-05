package com.example.documentintelligence.infrastructure.adapter;

import com.jayway.jsonpath.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processor for handling JSONPath expressions with token replacement and recursive resolution.
 */
@Slf4j
public class JsonPathProcessor {

    private static final String TOKEN_REGEX = "\\{[^#]+}";
    private static final int MAX_RECURSION_DEPTH = 100;
    private final ReentrantLock lock = new ReentrantLock();

    public static final Configuration config = Configuration.builder()
                                                            .options(Option.ALWAYS_RETURN_LIST)
                                                            .build();

    public static <T> List<T> readPathAsList(String json, String path) {
        new TypeRef<List<T>>() {};
        return JsonPath.using(config).parse(json).read(path, new TypeRef<List<T>>() {});
    }

    /**
     * Replaces tokens in a JSONPath expression using extracted values.
     */
    public static List<String> replacePendingTokens(String jsonPath, String content, Map<String, String> pendingJsonPaths) {
        log.info("Starting token replacement for JSONPath: {}", jsonPath);
        JsonPathProcessor processor = new JsonPathProcessor();
        Map<String, List<String>> resolvedTokens = processor.expandJsonPathsIteratively(pendingJsonPaths, content);

        log.info("Final resolved tokens: {}", resolvedTokens);
        return generateJsonPathCombinations(jsonPath, resolvedTokens);
    }

    /**
     * Generates all possible JSONPath combinations.
     */
    private static List<String> generateJsonPathCombinations(String template, Map<String, List<String>> tokenMappings) {
        List<String> results = new ArrayList<>(Collections.singletonList(template));

        for (Map.Entry<String, List<String>> entry : tokenMappings.entrySet()) {
            log.debug("Replacing token: {} with values: {}", entry.getKey(), entry.getValue());
            List<String> tempResults = new ArrayList<>();
            for (String partial : results) {
                for (String value : entry.getValue()) {
                    tempResults.add(partial.replace(entry.getKey(), value));
                }
            }
            results = tempResults;
        }
        log.debug("Generated {} combinations.", results.size());
        return results;
    }

    /**
     * Iterative method to resolve JSONPath expressions and prevent StackOverflow.
     */
    private Map<String, List<String>> expandJsonPathsIteratively(Map<String, String> pendingPaths, String documentData) {
        Deque<Map<String, String>> stack = new ArrayDeque<>();
        stack.push(new LinkedHashMap<>(pendingPaths));

        Map<String, List<String>> expandedPaths = new HashMap<>();

        int depth = 0;
        while (!stack.isEmpty() && depth <= MAX_RECURSION_DEPTH) {
            Map<String, String> currentPaths = stack.pop();
            Map<String, String> unresolvedPaths = new LinkedHashMap<>(currentPaths);

            for (Map.Entry<String, String> entry : currentPaths.entrySet()) {
                List<String> extractedValues = extractValuesFromJson(entry.getValue(), documentData, expandedPaths);
                if (!extractedValues.isEmpty()) {
                    expandedPaths.put(entry.getKey(), extractedValues);
                    unresolvedPaths.remove(entry.getKey());
                }
            }

            if (!unresolvedPaths.isEmpty()) {
                stack.push(unresolvedPaths);
            }
            depth++;
        }

        if (depth > MAX_RECURSION_DEPTH) {
            log.warn("Max recursion depth reached, some paths might be unresolved.");
        }

        return expandedPaths;
    }

    /**
     * Extracts values from JSON using JSONPath.
     */
    private List<String> extractValuesFromJson(String jsonPath, String documentData, Map<String, List<String>> expandedPaths) {
        List<String> resolvedPaths = resolveTokensInPath(jsonPath, expandedPaths);
        List<String> extractedValues = new ArrayList<>();

        for (String path : resolvedPaths) {
            try {
                List<?> extractedData = JsonPath.using(config).parse(documentData).read(path);
                extractedData.forEach(value -> extractedValues.add(String.valueOf(value)));
            } catch (Exception e) {
                log.error("Invalid JSONPath expression: {} - Error: {}", path, e.getMessage());
            }
        }
        log.debug("Extracted {} values for path {}", extractedValues.size(), jsonPath);
        return extractedValues;
    }

    /**
     * Resolves tokens in JSONPath expressions.
     */
    private List<String> resolveTokensInPath(String path, Map<String, List<String>> tokenMappings) {
        List<String> resolvedPaths = new ArrayList<>();
        Matcher matcher = Pattern.compile(TOKEN_REGEX).matcher(path);
        boolean hasTokens = false;

        StringBuilder updatedPath = new StringBuilder(path);
        while (matcher.find()) {
            hasTokens = true;
            String token = matcher.group(0);
            List<String> values = tokenMappings.getOrDefault(token, Collections.emptyList());

            if (!values.isEmpty()) {
                for (String value : values) {
                    resolvedPaths.add(updatedPath.toString().replace(token, value));
                }
            }
        }

        if (!hasTokens) {
            // Validate JSONPath syntax safely
            try {
                JsonPath.using(config).parse(path);
            } catch (Exception e) {
                log.error("Invalid JSONPath detected: {} - Error: {}", path, e.getMessage());
            }
            return Collections.singletonList(path);
        }

        return resolvedPaths.isEmpty() ? Collections.singletonList(path) : resolvedPaths;
    }
}