package com.cinema.screening.infrastructure.web;

import com.cinema.screening.domain.Seat;
import com.cinema.screening.domain.Screening;

import java.util.List;
import java.util.UUID;

public record SeatsResponse(UUID screeningId, List<SeatDto> seats) {

    public record SeatDto(int row, int number, String status) {
        public static SeatDto from(Seat seat) {
            return new SeatDto(seat.row(), seat.number(), seat.status().name());
        }
    }

    public static SeatsResponse from(Screening screening) {
        return new SeatsResponse(
                screening.id().value(),
                screening.seats().stream().map(SeatDto::from).toList()
        );
    }
}
