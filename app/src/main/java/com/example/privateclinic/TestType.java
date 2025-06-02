package com.example.privateclinic;

import java.util.ArrayList;
import java.util.List;

import java.util.List;

public class TestType {
    private String id;
    private String name;
    private String description;
    private List<Integer> availableDays;
    private int startHour;
    private int endHour;
    private String preparation;
    private String price;

    // Пустой конструктор необходим для Firestore
    public TestType() {}

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Integer> getAvailableDays() {
        return availableDays != null ? availableDays : new ArrayList<>();
    }

    public void setAvailableDays(List<Integer> availableDays) {
        this.availableDays = availableDays;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}