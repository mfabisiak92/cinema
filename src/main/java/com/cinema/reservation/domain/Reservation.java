package com.cinema.reservation.domain;

import com.cinema.screening.domain.Seat;
import com.cinema.screening.domain.ScreeningId;
import com.cinema.shared.domain.AggregateRoot;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class Reservation extends AggregateRoot {

    private ReservationId id;
    private ScreeningId screeningId;
    private CustomerId customerId;
    private List<Seat> seats;
    private LocalDateTime expiresAt;
    private ReservationStatus status;

    private Reservation() {}

    public Reservation(
            ReservationId id,
            ScreeningId screeningId,
            CustomerId customerId,
            List<Seat> seats,
            LocalDateTime expiresAt
    ) {
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("Reservation must contain at least one seat");
        }
        List<Seat> unavailable = seats.stream().filter(s -> !s.isAvailable()).toList();
        if (!unavailable.isEmpty()) {
            throw new IllegalArgumentException("Cannot reserve seats that are unavailable: " + unavailable);
        }
        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expiry time must be in the future");
        }
        this.id = id;
        this.screeningId = screeningId;
        this.customerId = customerId;
        this.seats = List.copyOf(seats);
        this.expiresAt = expiresAt;
        this.status = ReservationStatus.PENDING;
        registerEvent(SeatsReserved.of(this));
    }

    public static Reservation reconstitute(
            ReservationId id,
            ScreeningId screeningId,
            CustomerId customerId,
            List<Seat> seats,
            LocalDateTime expiresAt,
            ReservationStatus status
    ) {
        var reservation = new Reservation();
        reservation.id = id;
        reservation.screeningId = screeningId;
        reservation.customerId = customerId;
        reservation.seats = List.copyOf(seats);
        reservation.expiresAt = expiresAt;
        reservation.status = status;
        return reservation;
    }

    public void confirm() {
        if (status == ReservationStatus.EXPIRED) {
            throw new IllegalStateException("Cannot confirm an expired reservation");
        }
        status = ReservationStatus.CONFIRMED;
    }

    public void expire() {
        if (status == ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot expire an already confirmed reservation");
        }
        status = ReservationStatus.EXPIRED;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    public ReservationId id() { return id; }
    public ScreeningId screeningId() { return screeningId; }
    public CustomerId customerId() { return customerId; }
    public List<Seat> seats() { return Collections.unmodifiableList(seats); }
    public LocalDateTime expiresAt() { return expiresAt; }
    public ReservationStatus status() { return status; }
}
