package com.cinema.reservation.infrastructure.persistence;

import com.cinema.screening.domain.Seat;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
class ReservationSeatEmbeddable {

    @Column(name = "row_number")
    private int row;

    @Column(name = "seat_number")
    private int number;

    protected ReservationSeatEmbeddable() {}

    ReservationSeatEmbeddable(int row, int number) {
        this.row = row;
        this.number = number;
    }

    static ReservationSeatEmbeddable from(Seat seat) {
        return new ReservationSeatEmbeddable(seat.row(), seat.number());
    }

    Seat toDomain() {
        return new Seat(row, number);
    }
}
