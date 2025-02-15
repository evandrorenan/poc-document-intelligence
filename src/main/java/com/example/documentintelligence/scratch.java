package com.example.documentintelligence;

import com.jayway.jsonpath.JsonPath;

import java.util.ArrayList;
import java.util.List;

public class scratch {

    public static void main(String[] args) {
        new scratch().process();
    }

    private void process() {
        UserRequest userRequest = new UserRequest();
        List<String> fieldsToValidatePath = prepareFieldsToValidatePath(userRequest);

        fieldsToValidatePath.forEach(fieldToValidatePath -> {
            try {
                Object content = JsonPath.read(userRequest.getUserContent(), fieldToValidatePath);
                Object docContent = JsonPath.read(loadDocumentContent(), fieldToValidatePath);

                if (!content.getClass().equals(docContent.getClass())) {
                    System.out.println("Type doesn't match");
                    return;
                }

                if (isPrimitiveType(content) && !content.equals(docContent)) {
                    System.out.println("Content doesn't match");
                    return;
                }

                if (isPrimitiveType(content)) {
                    System.out.println("MATCH " + content + " filter: " + fieldToValidatePath);
                    return;
                }

                List<String> contentList = (List<String>) content;
                List<String> docContentList = (List<String>) docContent;

                List<String> unmatchedContent = new ArrayList<>(contentList);
                contentList.forEach(c -> {
                    if (docContentList.contains(c)) {
                        System.out.println("MATCH item: " + c + " filter: " + fieldToValidatePath);
                        docContentList.remove(c);
                        unmatchedContent.remove(c);
                    }
                });

                printUnmatchedItems("Not found on doc: ", unmatchedContent);
                printUnmatchedItems("Not found on user data: ", docContentList);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private List<String> prepareFieldsToValidatePath(UserRequest userRequest) {
        List<String> fieldsToValidatePaths = new ArrayList<>();

        if (userRequest.getMatchParams().getPathsToObjectKey().isEmpty()) {
            return List.of(userRequest.getMatchParams().pathToFieldContent);
        }

        String pathToObjectKey = userRequest.getMatchParams().getPathsToObjectKey();
        Object objectKeys = JsonPath.read(userRequest.getUserContent(), pathToObjectKey);

        if (isPrimitiveType(objectKeys)) {
            return List.of(userRequest.getMatchParams().getPathToFieldContent());
        }

        if (objectKeys instanceof List<?> userContentObjectKeys) {
            userContentObjectKeys.forEach(key -> {
                String pathToContent = createFilter(userRequest.getMatchParams().getPathToFieldContent(), pathToObjectKey, key);
                fieldsToValidatePaths.add(pathToContent);
            });
        }

        fieldsToValidatePaths.forEach(System.out::println);
        return fieldsToValidatePaths;
    }

    private boolean isPrimitiveType(Object object) {
        return object instanceof String || object instanceof Boolean || object instanceof Integer || object instanceof Double;
    }

    private String createFilter(String fieldContentJsonPath, String jsonPathKey, Object item) {
        int lastDotIndex = jsonPathKey.lastIndexOf(".");
        if (lastDotIndex == -1) return null;
        String fieldName = jsonPathKey.substring(lastDotIndex);

        String[] splitJsonPaths = fieldContentJsonPath.split("\\*", 2);
        if (splitJsonPaths.length < 2) return null;

        return splitJsonPaths[0] + "?(@" + fieldName + " == " + item + ")" + splitJsonPaths[1];
    }

    private void printUnmatchedItems(String message, List<String> items) {
        System.out.println(message);
        items.forEach(System.out::println);
    }

    private String loadUserContent() {
        return """
                {
                  "propriedadeUrbana": [
                    {
                      "proprietarios": [
                        {"seq": 0, "nome": "Maria"},
                        {"seq": 1, "nome": "José"},
                        {"seq": 3, "nome": "Benedita"}
                      ],
                      "area": "40 metros",
                      "logradouro": "rua das alegrias, número 10",
                      "bairro": "jardim rosa",
                      "cidade": "São Paulo",
                      "uf": "SP",
                      "dataMatricula": null,
                      "numeroMatricula": 123,
                      "valorVenda": null
                    }
                  ],
                  "cpfCnpj": 123
                }
                """;
    }

    private String loadDocumentContent() {
        return """
                { "cpfCnpj": 123,
                  "propriedadeUrbana": [
                    {
                      "proprietarios": [
                        {"seq": 0, "nome": "Claudia"},
                        {"seq": 1, "nome": "José"},
                        {"seq": 2, "nome": "Maria"},
                        {"seq": 3, "nome": "Eva"}
                      ],
                      "area": "40 metros",
                      "logradouro": "rua das alegrias, número 10",
                      "bairro": "jardim rosario",
                      "cidade": "São Paulo",
                      "uf": "SP",
                      "dataMatricula": null,
                      "numeroMatricula": 123,
                      "valorVenda": 10000
                    }
                  ]
                }
                """;
    }

    class UserRequest {
        private final String userContent = loadUserContent();
        private final MatchParams matchParams = new MatchParams();

        public String getUserContent() {
            return userContent;
        }

        public MatchParams getMatchParams() {
            return matchParams;
        }
    }

    class MatchParams {
        private final String pathToFieldContent = "$.cpfCnpj";
        private final String pathsToObjectKey = "";

        public String getPathToFieldContent() {
            return pathToFieldContent;
        }

        public String getPathsToObjectKey() {
            return pathsToObjectKey;
        }
    }
}
