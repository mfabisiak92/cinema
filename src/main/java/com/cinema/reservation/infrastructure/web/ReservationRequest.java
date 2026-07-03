package com.cinema.reservation.infrastructure.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record ReservationRequest(
        @NotNull UUID screeningId,
        @NotNull UUID customerId,
        @NotEmpty List<SeatPositionDto> seats
) {
    public record SeatPositionDto(@Positive int row, @Positive int number) {}
}
