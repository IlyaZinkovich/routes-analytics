package com.routes.analyzer.repository;

import java.time.LocalDate;

public class Info {

    private String from;
    private LocalDate date;

    public Info(String from, LocalDate date) {
        this.from = from;
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public LocalDate getDate() {
        return date;
    }
}
