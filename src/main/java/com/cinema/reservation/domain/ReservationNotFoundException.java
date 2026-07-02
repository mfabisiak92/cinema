package com.cinema.reservation.domain;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(ReservationId id) {
        super("Reservation not found: " + id);
    }
}
