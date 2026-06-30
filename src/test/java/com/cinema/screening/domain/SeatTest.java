package com.cinema.screening.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeatTest {

    @Test
    void shouldCreateSeatWithValidRowAndNumber() {
        var seat = new Seat(1, 1);

        assertThat(seat.row()).isEqualTo(1);
        assertThat(seat.number()).isEqualTo(1);
    }

    @Test
    void shouldBeAvailableByDefault() {
        var seat = new Seat(1, 1);

        assertThat(seat.isAvailable()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "-1, 1", "1, 0", "1, -1", "0, 0"})
    void shouldRejectInvalidRowOrNumber(int row, int number) {
        assertThatThrownBy(() -> new Seat(row, number))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldEqualSeatsWithSameRowAndNumber() {
        var a = new Seat(3, 5);
        var b = new Seat(3, 5);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void shouldNotEqualSeatsWithDifferentPosition() {
        assertThat(new Seat(1, 2)).isNotEqualTo(new Seat(1, 3));
        assertThat(new Seat(1, 2)).isNotEqualTo(new Seat(2, 2));
    }

    @Test
    void shouldReserveAvailableSeat() {
        var seat = new Seat(1, 1);

        var reserved = seat.reserve();

        assertThat(reserved.status()).isEqualTo(SeatStatus.RESERVED);
    }

    @Test
    void shouldNotReserveAlreadyReservedSeat() {
        var seat = new Seat(1, 1).reserve();

        assertThatThrownBy(seat::reserve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already reserved");
    }

    @Test
    void shouldReleaseReservedSeat() {
        var seat = new Seat(1, 1).reserve();

        var released = seat.release();

        assertThat(released.isAvailable()).isTrue();
    }
}
