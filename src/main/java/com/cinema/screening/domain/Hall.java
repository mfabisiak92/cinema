package com.cinema.screening.domain;

import java.util.List;
import java.util.stream.IntStream;

public record Hall(String name, int rows, int seatsPerRow, List<Seat> seats) {

    public Hall {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Hall name must not be blank");
        if (rows < 1) throw new IllegalArgumentException("Rows must be >= 1");
        if (seatsPerRow < 1) throw new IllegalArgumentException("Seats per row must be >= 1");
        seats = List.copyOf(seats);
    }

    public Hall(String name, int rows, int seatsPerRow) {
        this(name, rows, seatsPerRow, generateSeats(rows, seatsPerRow));
    }

    public int capacity() {
        return rows * seatsPerRow;
    }

    private static List<Seat> generateSeats(int rows, int seatsPerRow) {
        return IntStream.rangeClosed(1, rows)
                .boxed()
                .flatMap(row -> IntStream.rangeClosed(1, seatsPerRow)
                        .mapToObj(number -> new Seat(row, number)))
                .toList();
    }
}
