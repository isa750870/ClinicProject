package com.example.privateclinic;


import com.google.firebase.firestore.PropertyName;

public class UserDoctorRelation {
    private String userId;
    private String doctorId;
    private boolean isFavorite;
    private boolean isBlocked;
    private String userNote;

    public UserDoctorRelation() {
        this.isFavorite = false;
        this.isBlocked = false;
        this.userNote = "";
    }

    // --- IS FAVORITE ---
    @PropertyName("isFavorite")
    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
        if (isFavorite) isBlocked = false;
    }

    // --- IS BLOCKED ---
    @PropertyName("isBlocked")
    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
        if (isBlocked) isFavorite = false;
    }

    // --- USER ID ---
    @PropertyName("userId")
    public String getUserId() {
        return userId != null ? userId : "";
    }

    public void setUserId(String userId) {
        this.userId = userId != null ? userId : "";
    }

    // --- DOCTOR ID ---
    @PropertyName("doctorId")
    public String getDoctorId() {
        return doctorId != null ? doctorId : "";
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId != null ? doctorId : "";
    }

    // --- USER NOTE ---
    @PropertyName("userNote")
    public String getUserNote() {
        return userNote != null ? userNote : "";
    }

    public void setUserNote(String userNote) {
        this.userNote = userNote != null ? userNote : "";
    }
}