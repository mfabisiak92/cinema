package com.cinema.screening.infrastructure.web;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record ScreeningRequest(
        @NotBlank String movieTitle,
        @NotNull HallRequest hall,
        @NotNull @Future LocalDateTime startTime
) {
    public record HallRequest(
            @NotBlank String name,
            @Positive int rows,
            @Positive int seatsPerRow
    ) {}
}
