package com.tegel.model;

import java.time.LocalDateTime;

/**
 * Model class for announcement entities
 */
public class Announcement {
    private int id; // maps to announcement_id in database
    private String title;
    private String content;
    private LocalDateTime createdAt; // maps to postedat in database
    private int postedBy; // maps to postedby in database
    private boolean isPublic; // maps to ispublic in database

    // Default constructor
    public Announcement() {
    }

    // Constructor with all fields
    public Announcement(int id, String title, String content, LocalDateTime createdAt,
                       int postedBy, boolean isPublic) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.postedBy = postedBy;
        this.isPublic = isPublic;
    }

    // Getters and setters
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(int postedBy) {
        this.postedBy = postedBy;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public String toString() {
        return "Announcement{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", postedBy=" + postedBy +
                ", isPublic=" + isPublic +
                '}';
    }
}
