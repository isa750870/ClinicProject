package com.example.privateclinic.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Doctor implements Parcelable {
    private String id;
    private String name;
    private String specialization;
    private String description;
    private double rating;
    private int reviewCount;
    private boolean isFavorite;
    private boolean isBlocked;
    private List<String> availableDays; // 1-пн, 2-вт,...,7-вс
    private int startHour; // Начало приема
    private int endHour;

    public Doctor() {}

    protected Doctor(Parcel in) {
        id = in.readString();
        name = in.readString();
        specialization = in.readString();
        description = in.readString();
        rating = in.readDouble();
        reviewCount = in.readInt();
        availableDays = in.createStringArrayList();
        startHour = in.readInt();
        endHour = in.readInt();
    }

    public static final Creator<Doctor> CREATOR = new Creator<Doctor>() {
        @Override
        public Doctor createFromParcel(Parcel in) {
            return new Doctor(in);
        }

        @Override
        public Doctor[] newArray(int size) {
            return new Doctor[size];
        }
    };

    // Геттеры
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public String getDescription() { return description; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
    public List<String> getAvailableDays() { return availableDays; }
    public int getStartHour() { return startHour; }
    public int getEndHour() { return endHour; }

    // Сеттеры
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setDescription(String description) { this.description = description; }
    public void setRating(double rating) { this.rating = rating; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public void setAvailableDays(List<String> availableDays) { this.availableDays = availableDays; }
    public void setStartHour(int startHour) { this.startHour = startHour; }
    public void setEndHour(int endHour) { this.endHour = endHour; }
    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(specialization);
        dest.writeString(description);
        dest.writeDouble(rating);
        dest.writeInt(reviewCount);
        dest.writeStringList(availableDays);
        dest.writeInt(startHour);
        dest.writeInt(endHour);
    }
}