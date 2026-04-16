package com.sentiment.youtube.model;

public class Comment {
    private String id;
    private String text;
    private String author;
    private int likes;
    private String timestamp;
    private int sentiment; // -1: negative, 0: neutral, 1: positive

    public Comment() {}

    public Comment(String id, String text, String author, int likes, String timestamp, int sentiment) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.likes = likes;
        this.timestamp = timestamp;
        this.sentiment = sentiment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getSentiment() {
        return sentiment;
    }

    public void setSentiment(int sentiment) {
        this.sentiment = sentiment;
    }
}
