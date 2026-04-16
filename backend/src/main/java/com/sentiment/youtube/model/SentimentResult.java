package com.sentiment.youtube.model;

import java.util.List;

/**
 * Result model for YouTube Comment Extraction
 * (Keeping legacy name for endpoint compatibility)
 */
public class SentimentResult {
    private int totalComments;
    private List<String> topComments;

    // Dummy fields for backward compatibility if needed by frontend
    private double positivePercentage = 0.0;
    private double negativePercentage = 0.0;
    private double neutralPercentage = 0.0;
    private int positiveCount = 0;
    private int negativeCount = 0;
    private int neutralCount = 0;

    // LLM Analysis Fields
    private String summary;
    private int toxicityScore;
    private List<String> keyTopics;

    public SentimentResult() {}

    public SentimentResult(int totalComments, List<String> topComments) {
        this.totalComments = totalComments;
        this.topComments = topComments;
    }

    public int getTotalComments() { return totalComments; }
    public void setTotalComments(int totalComments) { this.totalComments = totalComments; }
    public List<String> getTopComments() { return topComments; }
    public void setTopComments(List<String> topComments) { this.topComments = topComments; }

    // Getters and Setters for analytics fields
    public double getPositivePercentage() { return positivePercentage; }
    public void setPositivePercentage(double positivePercentage) { this.positivePercentage = positivePercentage; }
    
    public double getNegativePercentage() { return negativePercentage; }
    public void setNegativePercentage(double negativePercentage) { this.negativePercentage = negativePercentage; }
    
    public double getNeutralPercentage() { return neutralPercentage; }
    public void setNeutralPercentage(double neutralPercentage) { this.neutralPercentage = neutralPercentage; }
    
    public int getPositiveCount() { return positiveCount; }
    public void setPositiveCount(int positiveCount) { this.positiveCount = positiveCount; }
    
    public int getNegativeCount() { return negativeCount; }
    public void setNegativeCount(int negativeCount) { this.negativeCount = negativeCount; }
    
    public int getNeutralCount() { return neutralCount; }
    public void setNeutralCount(int neutralCount) { this.neutralCount = neutralCount; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public int getToxicityScore() { return toxicityScore; }
    public void setToxicityScore(int toxicityScore) { this.toxicityScore = toxicityScore; }

    public List<String> getKeyTopics() { return keyTopics; }
    public void setKeyTopics(List<String> keyTopics) { this.keyTopics = keyTopics; }
}
