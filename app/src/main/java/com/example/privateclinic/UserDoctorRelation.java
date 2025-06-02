package com.example.privateclinic;

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

    // Геттеры и сеттеры с улучшенной логикой
    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
        if (favorite) {
            this.isBlocked = false; // Автоматическое снятие из ЧС
        }
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        this.isBlocked = blocked;
        if (blocked) {
            this.isFavorite = false; // Автоматическое снятие из избранного
        }
    }

    // Геттеры
    public String getUserId() {
        return userId != null ? userId : "";
    }

    public String getDoctorId() {
        return doctorId != null ? doctorId : "";
    }

    // Сеттеры с защитой от null
    public void setUserId(String userId) {
        this.userId = userId != null ? userId : "";
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId != null ? doctorId : "";
    }
    public String getUserNote() { return userNote != null ? userNote : ""; }

    // Сеттеры
    public void setUserNote(String userNote) {
        this.userNote = userNote != null ? userNote : "";
    }
}