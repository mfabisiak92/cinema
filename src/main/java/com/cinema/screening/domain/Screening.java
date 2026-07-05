package com.cinema.screening.domain;

import com.cinema.shared.domain.AggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Screening extends AggregateRoot {

    private ScreeningId id;
    private String movieTitle;
    private Hall hall;
    private LocalDateTime startTime;
    private List<Seat> seats;

    private Screening() {}

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

    public static Screening reconstitute(
            ScreeningId id, String movieTitle, Hall hall, LocalDateTime startTime, List<Seat> seats) {
        var screening = new Screening();
        screening.id = id;
        screening.movieTitle = movieTitle;
        screening.hall = hall;
        screening.startTime = startTime;
        screening.seats = new ArrayList<>(seats);
        return screening;
    }

    public void reserveSeat(int row, int number) {
        int index = findSeatIndex(row, number);
        seats.set(index, seats.get(index).reserve());
    }

    public void releaseSeat(int row, int number) {
        int index = findSeatIndex(row, number);
        seats.set(index, seats.get(index).release());
    }

    private int findSeatIndex(int row, int number) {
        for (int i = 0; i < seats.size(); i++) {
            Seat s = seats.get(i);
            if (s.row() == row && s.number() == number) return i;
        }
        throw new IllegalArgumentException("Seat [%d,%d] not found in screening %s".formatted(row, number, id));
    }

    public ScreeningId id() { return id; }
    public String movieTitle() { return movieTitle; }
    public Hall hall() { return hall; }
    public LocalDateTime startTime() { return startTime; }
    public List<Seat> seats() { return Collections.unmodifiableList(seats); }
}
