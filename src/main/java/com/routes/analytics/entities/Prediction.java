package com.routes.analytics.entities;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

public class Prediction {

    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd")
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
