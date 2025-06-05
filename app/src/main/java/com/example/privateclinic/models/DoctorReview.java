package com.example.privateclinic.models;

import com.google.firebase.Timestamp;

public class DoctorReview {
    private String id;
    private String doctorId;
    private String userId;
    private String userName;
    private String reviewText;
    private int rating;
    private Timestamp createdAt;

    public DoctorReview() {}

    // Геттеры
    public String getId() { return id; }
    public String getDoctorId() { return doctorId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getReviewText() { return reviewText; }
    public int getRating() { return rating; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Сеттеры
    public void setId(String id) { this.id = id; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public void setRating(int rating) { this.rating = rating; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}