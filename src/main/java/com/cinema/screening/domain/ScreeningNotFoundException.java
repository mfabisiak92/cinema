package com.cinema.screening.domain;

public class ScreeningNotFoundException extends RuntimeException {
    public ScreeningNotFoundException(ScreeningId id) {
        super("Screening not found: " + id);
    }
}
