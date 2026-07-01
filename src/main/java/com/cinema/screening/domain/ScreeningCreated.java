package com.cinema.screening.domain;

import com.cinema.shared.domain.DomainEvent;

import java.time.Instant;
import java.time.LocalDateTime;

public record ScreeningCreated(
        ScreeningId screeningId,
        String movieTitle,
        LocalDateTime startTime,
        Instant occurredOn
) implements DomainEvent {

    public static ScreeningCreated of(Screening screening) {
        return new ScreeningCreated(
                screening.id(),
                screening.movieTitle(),
                screening.startTime(),
                Instant.now()
        );
    }
}
