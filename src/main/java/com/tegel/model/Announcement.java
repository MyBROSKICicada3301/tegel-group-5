package com.tegel.model;

import java.time.LocalDateTime;

/**
 * Model class representing an announcement in the system.
 */
public class Announcement {
    private int announcementId;
    private String title;
    private String content;
    private int postedBy;
    private LocalDateTime postedAt;
    private boolean isPublic;

    // Default constructor
    public Announcement() {
    }

    // Constructor with all fields
    public Announcement(int announcementId, String title, String content,
                      int postedBy, LocalDateTime postedAt, boolean isPublic) {
        this.announcementId = announcementId;
        this.title = title;
        this.content = content;
        this.postedBy = postedBy;
        this.postedAt = postedAt;
        this.isPublic = isPublic;
    }

    // Getters and setters
    public int getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(int announcementId) {
        this.announcementId = announcementId;
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

    public int getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(int postedBy) {
        this.postedBy = postedBy;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = postedAt;
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
                "announcementId=" + announcementId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", postedBy=" + postedBy +
                ", postedAt=" + postedAt +
                ", isPublic=" + isPublic +
                '}';
    }
}
