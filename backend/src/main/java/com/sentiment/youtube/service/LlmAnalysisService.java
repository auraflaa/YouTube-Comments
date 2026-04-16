package com.sentiment.youtube.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LlmAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(LlmAnalysisService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class LlmResult {
        public String summary;
        public int toxicityScore;
        public double positivePercentage;
        public double negativePercentage;
        public double neutralPercentage;
        public List<String> keyTopics = new ArrayList<>();
    }

    public LlmResult analyzeComments(List<String> comments, String fallbackApiKey) {
        String googleApiKey = System.getenv("GOOGLE_API_KEY");
        if (googleApiKey == null || googleApiKey.trim().isEmpty()) {
            // Fallback to key provided in the request
            googleApiKey = fallbackApiKey;
        }
        
        if (googleApiKey == null || googleApiKey.trim().isEmpty()) {
            // Final fallback to YouTube environment variable
            googleApiKey = System.getenv("YOUTUBE_API_KEY");
        }

        if (googleApiKey == null || googleApiKey.trim().isEmpty()) {
            logger.warn("No Google API Key found. Returning generic fallback.");
            return createSafeFallback();
        }

        try {
            // Prompt construction
            String joinedComments = String.join("\n- ", comments.subList(0, Math.min(40, comments.size())));
            
            String prompt = "Act as a sentiment analysis agent. Analyze the following YouTube comments.\n" +
                            "Output MUST be a single, valid JSON object with NO additional text, NO markdown, and NO code blocks.\n\n" +
                            "Required JSON structure:\n" +
                            "{\n" +
                            "  \"summary\": \"(2 sentence summary)\",\n" +
                            "  \"toxicityScore\": (integer 0-100),\n" +
                            "  \"positivePercentage\": (double 0-100),\n" +
                            "  \"negativePercentage\": (double 0-100),\n" +
                            "  \"neutralPercentage\": (double 0-100),\n" +
                            "  \"keyTopics\": [\"topic1\", \"topic2\", \"topic3\"]\n" +
                            "}\n\n" +
                            "Comments to analyze:\n- " + joinedComments;

            // Using standard Google Generative Language API schema (Instruction-Tuned model)
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemma-4-31b-it:generateContent?key=" + googleApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = "{\n" +
                    "  \"contents\": [{\n" +
                    "    \"parts\":[{\"text\": " + objectMapper.writeValueAsString(prompt) + "}]\n" +
                    "  }]\n" +
                    "}";

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            logger.info("Sending request to Google API (Model: gemma-4-31b-it)...");
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            logger.info("Raw LLM Response received. Body: {}", response.getBody());
            
            return parseResponse(response.getBody());

        } catch (Exception e) {
            logger.error("Error analyzing comments with LLM: {}", e.getMessage(), e);
            return createSafeFallback(); // Fallback so the pipeline doesn't crash entirely if the model naming is restricted
        }
    }

    private LlmResult parseResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode candidate = root.path("candidates").get(0);
            if (candidate == null) return createSafeFallback();
            
            JsonNode parts = candidate.path("content").path("parts");
            String text = null;
            
            // Loop through all parts to find the one containing JSON
            if (parts.isArray()) {
                for (JsonNode part : parts) {
                    if (part.path("thought").asBoolean()) continue;
                    
                    String partText = part.path("text").asText();
                    if (partText.contains("{") && partText.contains("}")) {
                        text = partText;
                        break; 
                    }
                }
            }

            if (text == null) {
                text = parts.path(0).path("text").asText();
            }
            
            // Smarter JSON extraction: Find all blocks and pick the one that looks most like our result
            Matcher m = Pattern.compile("\\{.*?\\}", Pattern.DOTALL).matcher(text);
            String bestMatch = text;
            while (m.find()) {
                String match = m.group();
                if (match.contains("\"summary\"") || match.contains("\"toxicityScore\"")) {
                    bestMatch = match;
                }
            }
            text = bestMatch;

            JsonNode parsedObj = objectMapper.readTree(text);
            LlmResult res = new LlmResult();
            res.summary = parsedObj.path("summary").asText("No summary available.");
            res.toxicityScore = parsedObj.path("toxicityScore").asInt(0);
            res.positivePercentage = parsedObj.path("positivePercentage").asDouble(0.0);
            res.negativePercentage = parsedObj.path("negativePercentage").asDouble(0.0);
            res.neutralPercentage = parsedObj.path("neutralPercentage").asDouble(0.0);
            
            if (parsedObj.has("keyTopics") && parsedObj.path("keyTopics").isArray()) {
                for (JsonNode topicNode : parsedObj.path("keyTopics")) {
                    res.keyTopics.add(topicNode.asText());
                }
            } else {
                res.keyTopics.add("General feedback");
            }
            
            return res;

        } catch (Exception e) {
            logger.error("Failed to parse LLM JSON output", e);
            return createSafeFallback();
        }
    }

    private LlmResult createSafeFallback() {
        LlmResult fallback = new LlmResult();
        fallback.summary = "LLM Integration is currently restricted or model is not available.";
        fallback.toxicityScore = 0;
        fallback.positivePercentage = 50.0;
        fallback.neutralPercentage = 50.0;
        fallback.negativePercentage = 0.0;
        fallback.keyTopics.add("Error connecting to LLM");
        return fallback;
    }
}
