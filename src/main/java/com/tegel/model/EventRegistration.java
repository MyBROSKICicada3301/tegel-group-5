package com.tegel.model;

/**
 * Model class representing a user's registration for an event in the system.
 */
public class EventRegistration {
    private int registrationId;
    private int eventId;
    private int userId;
    private String dietaryRestrictions;
    private String status;
    private String specialRequests;
    private boolean wantsFoodOption;

    // Constants for registration status
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_WAITLISTED = "WAITLISTED";

    // Constructors
    public EventRegistration() {
    }

    public EventRegistration(int eventId, int userId) {
        this.eventId = eventId;
        this.userId = userId;
        this.status = STATUS_PENDING;
        this.wantsFoodOption = false;
    }

    public EventRegistration(int eventId, int userId, String dietaryRestrictions, String status,
                            String specialRequests, boolean wantsFoodOption) {
        this.eventId = eventId;
        this.userId = userId;
        this.dietaryRestrictions = dietaryRestrictions;
        this.status = status != null ? status : STATUS_PENDING;
        this.specialRequests = specialRequests;
        this.wantsFoodOption = wantsFoodOption;
    }

    // Getters and Setters
    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public boolean isWantsFoodOption() {
        return wantsFoodOption;
    }

    public void setWantsFoodOption(boolean wantsFoodOption) {
        this.wantsFoodOption = wantsFoodOption;
    }
}
