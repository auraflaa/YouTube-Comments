# YouTube Sentiment Analysis - Technical Report

This document details the data extraction pipeline and sentiment analysis logic.

---

## 🏗️ Data Pipeline

The system is designed to extract raw comment data from YouTube and transform it into structured sentiment insights.

### pipeline Architecture
```
[YouTube API Endpoint] ──(Extraction)──> [Backend Service] ──(Cleaning)──> [Sentiment Logic]
                                                                              │
                                                                       (Aggregation)
                                                                              │
                                                                       [Frontend UI]
```

### Extraction Workflow
1.  **Request**: Backend receives a YouTube URL.
2.  **Parsing**: Extracts the 11-character Video ID from the URL using Regex.
3.  **API Interaction**: Calls `commentThreads.list` via the YouTube Data API v3.
4.  **Transformation**: Maps raw JSON responses into internal `Comment` objects, stripping HTML tags and redundant metadata.

---

## 🔬 Sentiment Analysis Logic

The system uses a focused **lexicon-based algorithm** for real-time text classification.

### 1. Pre-Processing (Cleaning)
- **Normalization**: All text is converted to lowercase.
- **Punctuation Stripping**: Non-alphabetic characters are removed to focus on semantic keywords.

### 2. Matching
- **Keyword Search**: Matches the cleaned text directly against 200+ Positive and 200+ Negative keywords using string inclusion checks.
- **Simplicity**: No complex tokenization or NLP pipelines are used, keeping the service lightweight and fast.

### 3. Classification
Final sentiment is determined by comparing the aggregated Positive and Negative scores. Ties result in a **Neutral** classification.

---

## 📊 API Specification

### POST `/api/sentiment/analyze`
Analyzes a video's specific comment pool.

**Request**:
```json
{
  "videoUrl": "string",
  "apiKey": "string (optional)"
}
```

**Response**:
```json
{
  "totalComments": 100,
  "positiveCount": 45,
  "negativeCount": 20,
  "neutralCount": 35,
  "positivePercentage": 45.0
}
```
