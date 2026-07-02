package com.cinema.reservation.domain;

import com.cinema.screening.domain.Seat;
import com.cinema.screening.domain.ScreeningId;
import com.cinema.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.List;

public record SeatsReserved(
        ReservationId reservationId,
        ScreeningId screeningId,
        CustomerId customerId,
        List<Seat> seats,
        Instant occurredOn
) implements DomainEvent {

    public static SeatsReserved of(Reservation reservation) {
        return new SeatsReserved(
                reservation.id(),
                reservation.screeningId(),
                reservation.customerId(),
                List.copyOf(reservation.seats()),
                Instant.now()
        );
    }
}
