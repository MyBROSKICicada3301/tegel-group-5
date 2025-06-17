package com.tegel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Event {
    private int eventId;
    private String title;
    private String description;
    private LocalDate date;
    private String location;
    private String image;
    private int maxParticipants;
    private int currentParticipants;
    private int createdBy;
    private LocalDateTime createdAt;
    private boolean isActive;
    
    // Constructors
    public Event() {}
    
    public Event(String title, String description, LocalDate date, String location, 
                 String image, int maxParticipants, int createdBy, boolean isActive) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.image = image;
        this.maxParticipants = maxParticipants;
        this.createdBy = createdBy;
        this.isActive = isActive;
        this.currentParticipants = 0;
    }
    
    // Getters and Setters
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    
    public int getCurrentParticipants() { return currentParticipants; }
    public void setCurrentParticipants(int currentParticipants) { this.currentParticipants = currentParticipants; }
    
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
