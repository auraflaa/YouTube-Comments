package com.sentiment.youtube.service;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class YouTubeService {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeService.class);
    
    // Default maximum comments to fetch if not specified
    private static final int DEFAULT_MAX_COMMENTS = 100;
    private static final int RESULTS_PER_PAGE = 20;

    /**
     * Extract video ID from YouTube URL
     * Supported formats:
     * - https://www.youtube.com/watch?v=VIDEO_ID
     * - https://youtu.be/VIDEO_ID
     * - https://youtube.com/watch?v=VIDEO_ID
     * - https://www.youtube-nocookie.com/embed/VIDEO_ID
     */
    public String extractVideoId(String url) {
        String videoId = null;
        String pattern = "(?:youtube(?:-nocookie)?\\.com\\/(?:[^\\/]+\\/.+\\/|(?:v|e(?:mbed)?)\\/|.*[?&]v=)|youtu\\.be\\/)([^\"&?\\s]{11})";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        
        if (matcher.find()) {
            videoId = matcher.group(1);
        }
        
        logger.info("Extracted video ID: {}", videoId);
        return videoId;
    }

    /**
     * Fetch comments from YouTube API v3
     * Fetches up to 100 comments (at least 50 as required)
     * 
     * @param videoId The YouTube video ID
     * @param apiKey The YouTube Data API key
     * @param requestedMax The user-defined maximum comments
     * @return List of comment texts
     * @throws Exception if API call fails
     */
    public List<String> fetchYouTubeComments(String videoId, String apiKey, Integer requestedMax) throws Exception {
        List<String> comments = new ArrayList<>();
        
        int maxComments = (requestedMax != null && requestedMax > 0) ? requestedMax : DEFAULT_MAX_COMMENTS;
        
        // Use environment variable if no API key is provided in the request
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = System.getenv("YOUTUBE_API_KEY");
        }
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new Exception("No YouTube API key provided in the .env file. Please add YOUTUBE_API_KEY.");
        }
        
        try {
            // Initialize YouTube API client
            YouTube youtube = new YouTube.Builder(
                    new NetHttpTransport(),
                    new JacksonFactory(),
                    new HttpRequestInitializer() {
                        @Override
                        public void initialize(com.google.api.client.http.HttpRequest request) {
                            // No authentication needed for public data
                        }
                    }
            ).setApplicationName("youtube-sentiment-analysis").build();

            // Fetch comment threads
            YouTube.CommentThreads.List request = youtube.commentThreads()
                    .list(List.of("snippet"));
            
            request.setVideoId(videoId);
            request.setKey(apiKey);
            request.setMaxResults(Math.min(RESULTS_PER_PAGE, 20L));
            request.setTextFormat("plainText");
            request.setOrder("relevance");
            
            logger.info("Fetching comments for video ID: {} with API key", videoId);
            
            String nextPageToken = null;
            int totalComments = 0;
            
            // Fetch comments across multiple pages
            do {
                if (nextPageToken != null) {
                    request.setPageToken(nextPageToken);
                }
                
                CommentThreadListResponse response = request.execute();
                List<CommentThread> items = response.getItems();
                
                if (items != null && !items.isEmpty()) {
                    for (CommentThread item : items) {
                        String commentText = item.getSnippet()
                                .getTopLevelComment()
                                .getSnippet()
                                .getTextDisplay();

                        if (commentText != null && !commentText.trim().isEmpty()) {
                            comments.add(commentText);
                            totalComments++;

                            // Stop if we have enough comments
                            if (totalComments >= maxComments) {
                                break;
                            }
                        }
 
                        // Also fetch replies if available
                        int replyCount = item.getSnippet().getTotalReplyCount().intValue();
                        if (replyCount > 0 && totalComments < maxComments) {
                            List<String> replies = fetchReplies(youtube, item.getId(), apiKey);
                            comments.addAll(replies);
                            totalComments += replies.size();
 
                            if (totalComments >= maxComments) {
                                break;
                            }
                        }
                    }
                }
                
                nextPageToken = response.getNextPageToken();
                
                // Continue fetching if we need more comments
                if (totalComments >= maxComments || nextPageToken == null) {
                    break;
                }
                
            } while (nextPageToken != null && totalComments < maxComments);
            
            logger.info("Successfully fetched {} comments for video ID: {}", totalComments, videoId);
            
            // No longer padding with mock data, just returning whatever we successfully fetched.
            if (comments.isEmpty()) {
                logger.warn("Fetched 0 comments. The video might have comments disabled.");
            }
            
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            logger.error("Google API error: {} - {}", e.getStatusCode(), e.getStatusMessage());
            throw new Exception("YouTube API Error: API Key is invalid or blocked.", e);
        } catch (Exception e) {
            logger.error("Error fetching comments from YouTube API: {}", e.getMessage());
            throw new Exception("Error during fetching comments: " + e.getMessage(), e);
        }
        
        return comments;
    }

    /**
     * Fetch replies to a comment
     */
    private List<String> fetchReplies(YouTube youtube, String commentThreadId, String apiKey) throws Exception {
        List<String> replies = new ArrayList<>();
        
        try {
            YouTube.Comments.List replyRequest = youtube.comments()
                    .list(List.of("snippet"));
            
            replyRequest.setParentId(commentThreadId);
            replyRequest.setKey(apiKey);
            replyRequest.setMaxResults(5L);
            replyRequest.setTextFormat("plainText");
            
            com.google.api.services.youtube.model.CommentListResponse response = replyRequest.execute();
            
            if (response.getItems() != null) {
                for (com.google.api.services.youtube.model.Comment comment : response.getItems()) {
                    String replyText = comment.getSnippet().getTextDisplay();
                    if (replyText != null && !replyText.trim().isEmpty()) {
                        replies.add(replyText);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not fetch replies: {}", e.getMessage());
        }
        
        return replies;
    }
}

