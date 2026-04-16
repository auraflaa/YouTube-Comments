package com.sentiment.youtube.controller;

import com.sentiment.youtube.model.AnalysisRequest;
import com.sentiment.youtube.model.SentimentResult;
import com.sentiment.youtube.service.YouTubeService;
import com.sentiment.youtube.service.LlmAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/sentiment")
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:8080", "http://localhost:3000"})
public class SentimentAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(SentimentAnalysisController.class);

    @Autowired
    private YouTubeService youtubeService;

    @Autowired
    private LlmAnalysisService llmAnalysisService;

    /**
     * Discover comments from a YouTube video
     * (Reverted to legacy endpoint names for compatibility)
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeYouTubeComments(@RequestBody AnalysisRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Received extraction request for URL: {}", request.getVideoUrl());
            
            // Validate input
            if (request.getVideoUrl() == null || request.getVideoUrl().isEmpty()) {
                logger.warn("Video URL is empty");
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Video URL is required"));
            }

            // Extract video ID from URL
            String videoId = youtubeService.extractVideoId(request.getVideoUrl());
            if (videoId == null || videoId.isEmpty()) {
                logger.warn("Invalid YouTube URL format: {}", request.getVideoUrl());
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid YouTube URL format. Please provide a valid YouTube video URL."));
            }

            // Fetch comments from YouTube
            List<String> comments = youtubeService.fetchYouTubeComments(videoId, request.getApiKey(), request.getMaxComments());

            if (comments == null || comments.isEmpty()) {
                logger.warn("No comments found for video ID: {}", videoId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("No comments found for this video. The video may have comments disabled."));
            }

            // Create result object (Extraction focused)
            SentimentResult result = new SentimentResult();
            result.setTotalComments(comments.size());
            result.setTopComments(comments.subList(0, Math.min(40, comments.size())));

            // Trigger Gemma-4-31b Analysis
            logger.info("Sending {} comments to Gemma-4-31b for analysis...", comments.size());
            LlmAnalysisService.LlmResult llmResult = llmAnalysisService.analyzeComments(comments, request.getApiKey());
            
            result.setSummary(llmResult.summary);
            result.setToxicityScore(llmResult.toxicityScore);
            result.setKeyTopics(llmResult.keyTopics);
            result.setPositivePercentage(llmResult.positivePercentage);
            result.setNegativePercentage(llmResult.negativePercentage);
            result.setNeutralPercentage(llmResult.neutralPercentage);

            long endTime = System.currentTimeMillis();
            logger.info("Extraction complete. Time taken: {} ms. Extracted {} comments.",
                    (endTime - startTime), result.getTotalComments());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error extracting comments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error extracting comments: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("YouTube Comment Extraction Service is running!");
    }

    /**
     * Inner class for error responses
     */
    public static class ErrorResponse {
        public String error;
        public long timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
