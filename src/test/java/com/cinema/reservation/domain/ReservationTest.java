package com.cinema.reservation.domain;

import com.cinema.screening.domain.Seat;
import com.cinema.screening.domain.ScreeningId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private static final ReservationId ID = new ReservationId(UUID.randomUUID());
    private static final ScreeningId SCREENING_ID = new ScreeningId(UUID.randomUUID());
    private static final CustomerId CUSTOMER_ID = new CustomerId(UUID.randomUUID());
    private static final List<Seat> AVAILABLE_SEATS = List.of(new Seat(1, 1), new Seat(1, 2));
    private static final LocalDateTime EXPIRES_AT = LocalDateTime.now().plusMinutes(15);

    @Test
    void shouldCreateReservationWithValidData() {
        var reservation = new Reservation(ID, SCREENING_ID, CUSTOMER_ID, AVAILABLE_SEATS, EXPIRES_AT);

        assertThat(reservation.id()).isEqualTo(ID);
        assertThat(reservation.screeningId()).isEqualTo(SCREENING_ID);
        assertThat(reservation.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(reservation.seats()).containsExactlyElementsOf(AVAILABLE_SEATS);
        assertThat(reservation.status()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void shouldNotAllowReservingUnavailableSeats() {
        var reservedSeat = new Seat(1, 1).reserve();
        var seats = List.of(reservedSeat, new Seat(1, 2));

        assertThatThrownBy(() -> new Reservation(ID, SCREENING_ID, CUSTOMER_ID, seats, EXPIRES_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unavailable");
    }

    @Test
    void shouldNotAllowEmptySeatList() {
        assertThatThrownBy(() -> new Reservation(ID, SCREENING_ID, CUSTOMER_ID, List.of(), EXPIRES_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one seat");
    }

    @Test
    void shouldNotAllowExpiryInThePast() {
        var pastExpiry = LocalDateTime.now().minusMinutes(1);

        assertThatThrownBy(() -> new Reservation(ID, SCREENING_ID, CUSTOMER_ID, AVAILABLE_SEATS, pastExpiry))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @Test
    void shouldBeExpiredWhenExpiryTimeHasPassed() {
        var expiresInOneMinute = LocalDateTime.now().plusMinutes(1);
        var reservation = new Reservation(ID, SCREENING_ID, CUSTOMER_ID, AVAILABLE_SEATS, expiresInOneMinute);

        assertThat(reservation.isExpired(LocalDateTime.now().plusMinutes(2))).isTrue();
    }

    @Test
    void shouldNotBeExpiredBeforeExpiryTime() {
        var reservation = new Reservation(ID, SCREENING_ID, CUSTOMER_ID, AVAILABLE_SEATS, EXPIRES_AT);

        assertThat(reservation.isExpired(LocalDateTime.now())).isFalse();
    }

    @Test
    void shouldConfirmPendingReservation() {
        var reservation = new Reservation(ID, SCREENING_ID, CUSTOMER_ID, AVAILABLE_SEATS, EXPIRES_AT);

        reservation.confirm();

        assertThat(reservation.status()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void shouldNotConfirmExpiredReservation() {
        var reservation = new Reservation(ID, SCREENING_ID, CUSTOMER_ID, AVAILABLE_SEATS, EXPIRES_AT);
        reservation.expire();

        assertThatThrownBy(reservation::confirm)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void shouldExpirePendingReservation() {
        var reservation = new Reservation(ID, SCREENING_ID, CUSTOMER_ID, AVAILABLE_SEATS, EXPIRES_AT);

        reservation.expire();

        assertThat(reservation.status()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void shouldNotExpireAlreadyConfirmedReservation() {
        var reservation = new Reservation(ID, SCREENING_ID, CUSTOMER_ID, AVAILABLE_SEATS, EXPIRES_AT);
        reservation.confirm();

        assertThatThrownBy(reservation::expire)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("confirmed");
    }

    @Test
    void shouldEmitSeatsReservedEventOnCreation() {
        var reservation = new Reservation(ID, SCREENING_ID, CUSTOMER_ID, AVAILABLE_SEATS, EXPIRES_AT);

        var events = reservation.pullDomainEvents();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(SeatsReserved.class);

        var event = (SeatsReserved) events.getFirst();
        assertThat(event.reservationId()).isEqualTo(ID);
        assertThat(event.screeningId()).isEqualTo(SCREENING_ID);
        assertThat(event.seats()).containsExactlyElementsOf(AVAILABLE_SEATS);
    }

    @Test
    void shouldNotEmitEventsTwiceAfterPull() {
        var reservation = new Reservation(ID, SCREENING_ID, CUSTOMER_ID, AVAILABLE_SEATS, EXPIRES_AT);
        reservation.pullDomainEvents();

        assertThat(reservation.pullDomainEvents()).isEmpty();
    }
}
