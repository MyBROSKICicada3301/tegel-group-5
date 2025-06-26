package com.tegel.model;

import java.time.LocalDateTime;

/**
 * Model class representing a Newsletter
 */
public class Newsletter {
    private int id;
    private String title;
    private String content;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private boolean isPublished;

    // Default constructor
    public Newsletter() {
    }

    // Constructor with parameters
    public Newsletter(int id, String title, String content, int createdBy,
                     LocalDateTime createdAt, LocalDateTime publishedAt, boolean isPublished) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
        this.isPublished = isPublished;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    @Override
    public String toString() {
        return "Newsletter{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 30)) + "..." : "null") + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", publishedAt=" + publishedAt +
                ", isPublished=" + isPublished +
                '}';
    }
}
