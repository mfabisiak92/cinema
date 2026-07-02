package com.cinema.reservation.domain;

import java.util.UUID;

public record ReservationId(UUID value) {

    public ReservationId {
        if (value == null) throw new IllegalArgumentException("ReservationId must not be null");
    }

    public static ReservationId newId() {
        return new ReservationId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
