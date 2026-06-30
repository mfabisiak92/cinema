package com.cinema.screening.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HallTest {

    @Test
    void shouldCreateHallWithValidDimensions() {
        var hall = new Hall("A", 5, 10);

        assertThat(hall.name()).isEqualTo("A");
        assertThat(hall.rows()).isEqualTo(5);
        assertThat(hall.seatsPerRow()).isEqualTo(10);
    }

    @Test
    void shouldGenerateCorrectNumberOfSeats() {
        var hall = new Hall("A", 3, 4);

        assertThat(hall.seats()).hasSize(12);
    }

    @Test
    void shouldGenerateSeatsWithCorrectRowsAndNumbers() {
        var hall = new Hall("A", 2, 3);

        assertThat(hall.seats()).containsExactlyInAnyOrder(
                new Seat(1, 1), new Seat(1, 2), new Seat(1, 3),
                new Seat(2, 1), new Seat(2, 2), new Seat(2, 3)
        );
    }

    @Test
    void shouldReportTotalCapacity() {
        var hall = new Hall("B", 4, 8);

        assertThat(hall.capacity()).isEqualTo(32);
    }

    @ParameterizedTest
    @CsvSource({"0, 10", "-1, 10", "5, 0", "5, -1"})
    void shouldRejectInvalidDimensions(int rows, int seatsPerRow) {
        assertThatThrownBy(() -> new Hall("A", rows, seatsPerRow))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> new Hall("", 5, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Hall("  ", 5, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldEqualHallsWithSameName() {
        var a = new Hall("A", 5, 10);
        var b = new Hall("A", 5, 10);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
