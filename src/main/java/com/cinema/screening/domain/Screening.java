package com.cinema.screening.domain;

import com.cinema.shared.domain.AggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Screening extends AggregateRoot {

    private final ScreeningId id;
    private final String movieTitle;
    private final Hall hall;
    private final LocalDateTime startTime;
    private final List<Seat> seats;

    public Screening(ScreeningId id, String movieTitle, Hall hall, LocalDateTime startTime) {
        if (movieTitle == null || movieTitle.isBlank()) {
            throw new IllegalArgumentException("Movie title must not be blank");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Screening cannot be scheduled in the past");
        }
        this.id = id;
        this.movieTitle = movieTitle;
        this.hall = hall;
        this.startTime = startTime;
        this.seats = new ArrayList<>(hall.seats());
        registerEvent(ScreeningCreated.of(this));
    }

    public ScreeningId id() { return id; }
    public String movieTitle() { return movieTitle; }
    public Hall hall() { return hall; }
    public LocalDateTime startTime() { return startTime; }
    public List<Seat> seats() { return Collections.unmodifiableList(seats); }
}
