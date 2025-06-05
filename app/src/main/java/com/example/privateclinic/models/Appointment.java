package com.example.privateclinic.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

public class Appointment implements Parcelable {
    private String id;
    private String userId;
    private String doctorId;
    private String doctorName;
    private String specialization;
    private Timestamp appointmentTime;
    private Timestamp createdAt;
    private String status; // booked, completed, canceled

    public Appointment() {}

    protected Appointment(Parcel in) {
        id = in.readString();
        userId = in.readString();
        doctorId = in.readString();
        doctorName = in.readString();
        specialization = in.readString();
        appointmentTime = in.readParcelable(Timestamp.class.getClassLoader());
        createdAt = in.readParcelable(Timestamp.class.getClassLoader());
        status = in.readString();
    }

    public static final Creator<Appointment> CREATOR = new Creator<Appointment>() {
        @Override
        public Appointment createFromParcel(Parcel in) {
            return new Appointment(in);
        }

        @Override
        public Appointment[] newArray(int size) {
            return new Appointment[size];
        }
    };

    // Геттеры
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getSpecialization() { return specialization; }
    public Timestamp getAppointmentTime() { return appointmentTime; }
    public Timestamp getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }

    // Сеттеры
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setAppointmentTime(Timestamp appointmentTime) { this.appointmentTime = appointmentTime; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(doctorId);
        dest.writeString(doctorName);
        dest.writeString(specialization);
        dest.writeParcelable(appointmentTime, flags);
        dest.writeParcelable(createdAt, flags);
        dest.writeString(status);
    }
}