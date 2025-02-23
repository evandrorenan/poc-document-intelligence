package com.example.documentintelligence.infrastructure.adapter;

import com.jayway.jsonpath.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processor for handling JSONPath expressions with token replacement and recursive resolution.
 */
@Slf4j
public class JsonPathProcessor {

    private static final String TOKEN_REGEX = "\\{[^#]+}";
    private static final int MAX_RECURSION_DEPTH = 100;

    public static final Configuration config = Configuration.builder()
                                                            .options(Option.ALWAYS_RETURN_LIST)
                                                            .build();

    /**
     * Replaces tokens in a JSONPath expression using extracted values.
     *
     * @param jsonPath the JSONPath expression containing tokens
     * @param content the JSON document to extract values from
     * @param pendingJsonPaths map of tokens to their JSONPath expressions
     * @return a list of JSONPath expressions with replaced values
     */
    public static List<String> replacePendingTokens(String jsonPath, String content, Map<String, String> pendingJsonPaths) {
        log.info("Starting token replacement for JSONPath: {}", jsonPath);
        Map<String, List<String>> expandedJsonPaths = new LinkedHashMap<>();
        JsonPathProcessor processor = new JsonPathProcessor();
        Map<String, List<String>> resolvedTokens = processor.expandJsonPathsRecursively(pendingJsonPaths, content, expandedJsonPaths, 0);

        log.info("Final resolved tokens: {}", resolvedTokens);
        return generateJsonPathCombinations(jsonPath, resolvedTokens);
    }

    /**
     * Generates all possible JSONPath combinations by replacing tokens with resolved values.
     *
     * @param template the JSONPath template containing tokens
     * @param tokenMappings map of tokens and their resolved values
     * @return list of generated JSONPath expressions
     */
    private static List<String> generateJsonPathCombinations(String template, Map<String, List<String>> tokenMappings) {
        List<String> results = new ArrayList<>(Collections.singletonList(template));
        log.info("Generating JSONPath combinations for template: {}", template);

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
        log.info("Generated combinations: {}", results);
        return results;
    }

    /**
     * Recursively resolves JSONPath expressions by replacing tokens with extracted values.
     *
     * @param pendingPaths map of tokens and their unresolved JSONPath expressions
     * @param documentData the JSON document data
     * @param expandedPaths map to store resolved paths
     * @param depth current recursion depth
     * @return map of resolved tokens to extracted values
     */
    private synchronized Map<String, List<String>> expandJsonPathsRecursively(Map<String, String> pendingPaths, String documentData, Map<String, List<String>> expandedPaths, int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            log.warn("Max recursion depth reached ({}), stopping expansion to avoid infinite loop.", MAX_RECURSION_DEPTH);
            return expandedPaths;
        }

        log.info("Recursive call at depth {}: pendingPaths={}, expandedPaths={}", depth, pendingPaths, expandedPaths);
        Map<String, String> unresolvedPaths = new LinkedHashMap<>(pendingPaths);

        pendingPaths.forEach((key, value) -> {
            log.debug("Processing pending path: {} with expression: {}", key, value);
            List<String> extractedValues = extractValuesFromJson(value, documentData, expandedPaths);
            if (!extractedValues.isEmpty()) {
                expandedPaths.put(key, extractedValues);
                unresolvedPaths.remove(key);
                log.debug("Resolved {} to values: {}", key, extractedValues);
            }
        });

        if (unresolvedPaths.isEmpty()) {
            log.info("All paths resolved at depth {}: {}", depth, expandedPaths);
            return expandedPaths;
        }
        return expandJsonPathsRecursively(unresolvedPaths, documentData, expandedPaths, depth + 1);
    }

    /**
     * Extracts values from the JSON document based on the provided JSONPath expression.
     *
     * @param jsonPath the JSONPath expression
     * @param documentData the JSON document data
     * @param expandedPaths map of already resolved token mappings
     * @return list of extracted values
     */
    private synchronized List<String> extractValuesFromJson(String jsonPath, String documentData, Map<String, List<String>> expandedPaths) {
        log.debug("Extracting values for JSONPath: {}", jsonPath);
        List<String> resolvedPaths = resolveTokensInPath(jsonPath, expandedPaths);
        List<String> extractedValues = new LinkedList<>();

        resolvedPaths.forEach(path -> {
            log.debug("Evaluating JSONPath: {}", path);
            List<?> extractedData = JsonPath.using(config).parse(documentData).read(path);
            extractedData.forEach(value -> extractedValues.add(String.valueOf(value)));
        });
        log.info("Extracted values for path {}: {}", jsonPath, extractedValues);
        return extractedValues;
    }

    /**
     * Resolves tokens in the JSONPath expression using precomputed mappings.
     *
     * @param path the JSONPath expression containing tokens
     * @param tokenMappings map of token values
     * @return list of resolved JSONPath expressions
     */
    private synchronized List<String> resolveTokensInPath(String path, Map<String, List<String>> tokenMappings) {
        log.debug("Resolving tokens in path: {}", path);
        List<String> resolvedPaths = new LinkedList<>();
        Matcher matcher = Pattern.compile(TOKEN_REGEX).matcher(path);

        if (!matcher.find()) {
            log.warn("No tokens found in path, validating JSONPath syntax.");
            JsonPath.using(config).parse(path); // Will throw runtime exception if path is invalid
        }

        matcher.reset();
        while (matcher.find()) {
            String token = matcher.group(0);
            List<String> values = tokenMappings.getOrDefault(token, Collections.emptyList());
            log.debug("Replacing token {} with values: {}", token, values);
            values.forEach(value -> resolvedPaths.add(path.replace(token, value)));
        }

        log.info("Resolved paths: {}", resolvedPaths.isEmpty() ? Collections.singletonList(path) : resolvedPaths);
        return resolvedPaths.isEmpty() ? Collections.singletonList(path) : resolvedPaths;
    }
}
