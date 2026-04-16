package com.sentiment.youtube;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/sentiment";
    }

    @Test
    public void testHealthCheck() {
        ResponseEntity<String> response = restTemplate.getForEntity(getBaseUrl() + "/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Service is running");
    }

    @Test
    public void testAnalyzeSentimentDemoMode() {
        Map<String, Object> request = new HashMap<>();
        request.put("videoUrl", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        request.put("apiKey", null); // Trigger Demo/Mock Mode

        ResponseEntity<Map> response = restTemplate.postForEntity(getBaseUrl() + "/analyze", request, Map.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        
        assertThat(body).isNotNull();
        assertThat(body).containsKey("totalComments");
        assertThat(body).containsKey("positivePercentage");
        assertThat(body).containsKey("negativePercentage");
        assertThat(body).containsKey("neutralPercentage");
        
        // In demo mode, it should return at least 50 comments (as per project logic)
        Integer totalComments = (Integer) body.get("totalComments");
        assertThat(totalComments).isGreaterThanOrEqualTo(50);
    }

    @Test
    public void testInvalidUrlHandling() {
        Map<String, Object> request = new HashMap<>();
        request.put("videoUrl", "not-a-valid-url");
        request.put("apiKey", null);

        ResponseEntity<Map> response = restTemplate.postForEntity(getBaseUrl() + "/analyze", request, Map.class);
        
        // Should return a bad request or error format
        assertThat(response.getStatusCode().isError()).isTrue();
    }
}
