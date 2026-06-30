package com.cinema.screening.domain;

public record Seat(int row, int number, SeatStatus status) {

    public Seat {
        if (row < 1) throw new IllegalArgumentException("Row must be >= 1");
        if (number < 1) throw new IllegalArgumentException("Seat number must be >= 1");
    }

    public Seat(int row, int number) {
        this(row, number, SeatStatus.AVAILABLE);
    }

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    public Seat reserve() {
        if (status == SeatStatus.RESERVED) {
            throw new IllegalStateException("Seat [%d,%d] is already reserved".formatted(row, number));
        }
        return new Seat(row, number, SeatStatus.RESERVED);
    }

    public Seat release() {
        return new Seat(row, number, SeatStatus.AVAILABLE);
    }
}
