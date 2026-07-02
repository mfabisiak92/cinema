package com.cinema.reservation.domain;

import java.util.UUID;

public record CustomerId(UUID value) {

    public CustomerId {
        if (value == null) throw new IllegalArgumentException("CustomerId must not be null");
    }

    public static CustomerId newId() {
        return new CustomerId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
