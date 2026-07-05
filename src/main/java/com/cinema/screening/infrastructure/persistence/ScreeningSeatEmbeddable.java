package com.cinema.screening.infrastructure.persistence;

import com.cinema.screening.domain.Seat;
import com.cinema.screening.domain.SeatStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
class ScreeningSeatEmbeddable {

    @Column(name = "row_number")
    private int row;

    @Column(name = "seat_number")
    private int number;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SeatStatus status;

    protected ScreeningSeatEmbeddable() {}

    ScreeningSeatEmbeddable(int row, int number, SeatStatus status) {
        this.row = row;
        this.number = number;
        this.status = status;
    }

    static ScreeningSeatEmbeddable from(Seat seat) {
        return new ScreeningSeatEmbeddable(seat.row(), seat.number(), seat.status());
    }

    Seat toDomain() {
        return new Seat(row, number, status);
    }
}
