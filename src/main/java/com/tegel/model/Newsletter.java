package com.tegel.model;

import java.time.LocalDateTime;

public class Newsletter {
    private int newsletterId;
    private String title;
    private String content;
    private LocalDateTime publishDate;

    public Newsletter() {
    }

    public Newsletter(int newsletterId, String title, String content, LocalDateTime publishDate) {
        this.newsletterId = newsletterId;
        this.title = title;
        this.content = content;
        this.publishDate = publishDate;
    }

    public int getNewsletterId() {
        return newsletterId;
    }

    public void setNewsletterId(int newsletterId) {
        this.newsletterId = newsletterId;
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

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }
}
