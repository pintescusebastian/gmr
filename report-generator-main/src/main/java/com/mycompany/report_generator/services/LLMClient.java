package com.mycompany.report_generator.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.io.IOException;

/**
 * Client pentru interacțiunea cu modelul Google Gemini via API REST,
 * folosind Structured Output (Schema JSON).
 */
@Component
public class LLMClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String apiKey;
    private static final String MODEL_NAME = "gemini-2.5-flash-preview-09-2025";

    public LLMClient(
            @Value("${gemini.api.url}") String apiUrl,
            @Value("${gemini.api.key}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl + "?key=" + apiKey)
                .build();
    }

    public String generateReport(String prompt) {
        if (this.apiKey.contains("YOUR_GEMINI_API_KEY_HERE")) {
            return "Eroare: Cheia API Gemini nu este configurată în application.properties.";
        }

        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "OBJECT");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("diagnosis", objectMapper.createObjectNode().put("type", "STRING"));
        properties.set("riskLevel", objectMapper.createObjectNode().put("type", "STRING").put("description", "Nivelul de risc (Scăzut, Mediu, Înalt, Critic)"));
        properties.set("analysis", objectMapper.createObjectNode().put("type", "STRING"));

        ArrayNode requiredFields = objectMapper.createArrayNode();
        requiredFields.add("diagnosis").add("riskLevel").add("analysis");

        schema.set("properties", properties);
        schema.set("required", requiredFields);

        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.set("responseSchema", schema);


        ObjectNode contents = objectMapper.createObjectNode();
        contents.put("role", "user");
        ArrayNode parts = contents.putArray("parts");
        parts.addObject().put("text", prompt);

        ArrayNode contentsArray = objectMapper.createArrayNode();
        contentsArray.add(contents);

        ObjectNode jsonPayload = objectMapper.createObjectNode();
        jsonPayload.set("contents", contentsArray);
        jsonPayload.set("generationConfig", generationConfig);


        String payloadString;
        try {
            payloadString = objectMapper.writeValueAsString(jsonPayload);
        } catch (IOException e) {
            System.err.println("Eroare la serializarea payload-ului JSON: " + e.getMessage());
            return "Eroare internă la construirea cererii LLM.";
        }

        // 3. Efectuează cererea către Gemini
        Mono<String> responseMono = webClient.post()
                .header("Content-Type", "application/json")
                .bodyValue(payloadString)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    System.err.println("Eroare la apelul Gemini API. Eroare: " + e.getMessage());
                    return Mono.just("Eroare de generare: Serviciul Gemini API nu este disponibil.");
                });

        String rawResponse = responseMono.block();

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode textNode = root.at("/candidates/0/content/parts/0/text");

            if (textNode.isTextual()) {
                return textNode.asText();
            }

            if (root.has("error")) {
                System.err.println("Eroare Gemini API: " + root.get("error").asText());
            }
            return "Format răspuns LLM neașteptat. Răspuns brut: " + rawResponse;
        } catch (IOException e) {
            System.err.println("Eroare la parsarea răspunsului Gemini. Verificați formatul: " + e.getMessage());
            return "Eroare la parsarea răspunsului LLM. Răspuns brut: " + rawResponse;
        } catch (Exception e) {
            System.err.println("Excepție neașteptată în LLMClient: " + e.getMessage());
            return "Excepție în procesarea răspunsului LLM.";
        }
    }
}