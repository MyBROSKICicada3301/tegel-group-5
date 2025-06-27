package com.tegel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class representing a user in the system.
 */
public class User {
    private int userId;
    private String email;
    private String passwordHash;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private LocalDateTime createDate;
    private String dietRes;
    private String role;
    private String fullName;
    private String nickName;

    // Constructors
    public User() {
    }

    public User(String email, String passwordHash, String phoneNumber, LocalDate dateOfBirth,
                String dietRes, String fullName, String nickName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.dietRes = dietRes;
        this.fullName = fullName;
        this.nickName = nickName;
        this.role = "member";           //The default role will always be 'member'
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public String getDietRes() {
        return dietRes;
    }

    public void setDietRes(String dietRes) {
        this.dietRes = dietRes;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
