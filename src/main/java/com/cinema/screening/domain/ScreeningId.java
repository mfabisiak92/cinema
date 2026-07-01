package com.cinema.screening.domain;

import java.util.UUID;

public record ScreeningId(UUID value) {

    public ScreeningId {
        if (value == null) throw new IllegalArgumentException("ScreeningId must not be null");
    }

    public static ScreeningId newId() {
        return new ScreeningId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
