package com.routes.analytics.entities;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

public class Route {

    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private Place from;
    private Place to;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Place getFrom() {
        return from;
    }

    public void setFrom(Place from) {
        this.from = from;
    }

    public Place getTo() {
        return to;
    }

    public void setTo(Place to) {
        this.to = to;
    }
}
