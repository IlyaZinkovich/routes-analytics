package com.routes.analyzer.model;

import java.time.LocalDate;

public class Prediction {

    private LocalDate date;
    private String country;
    private Double count;

    public Prediction(LocalDate date, String country, Double count) {
        this.date = date;
        this.country = country;
        this.count = count;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCountry() {
        return country;
    }

    public Double getCount() {
        return count;
    }
}
