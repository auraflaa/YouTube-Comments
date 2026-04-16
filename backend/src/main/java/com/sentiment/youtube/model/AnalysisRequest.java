package com.sentiment.youtube.model;

public class AnalysisRequest {
    private String videoUrl;
    private String apiKey;
    private Integer maxComments;

    public AnalysisRequest() {}

    public AnalysisRequest(String videoUrl, String apiKey, Integer maxComments) {
        this.videoUrl = videoUrl;
        this.apiKey = apiKey;
        this.maxComments = maxComments;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Integer getMaxComments() {
        return maxComments;
    }

    public void setMaxComments(Integer maxComments) {
        this.maxComments = maxComments;
    }
}
