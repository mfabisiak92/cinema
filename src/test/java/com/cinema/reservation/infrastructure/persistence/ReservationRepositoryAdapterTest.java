package com.cinema.reservation.infrastructure.persistence;

import com.cinema.reservation.application.ReservationRepository;
import com.cinema.reservation.domain.CustomerId;
import com.cinema.reservation.domain.Reservation;
import com.cinema.reservation.domain.ReservationId;
import com.cinema.reservation.domain.ReservationStatus;
import com.cinema.screening.application.ScreeningRepository;
import com.cinema.screening.domain.Hall;
import com.cinema.screening.domain.Screening;
import com.cinema.screening.domain.ScreeningId;
import com.cinema.screening.domain.Seat;
import com.cinema.shared.infrastructure.PostgreSQLIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ReservationRepositoryAdapterTest extends PostgreSQLIntegrationTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ScreeningRepository screeningRepository;

    private ScreeningId screeningId;
    private final CustomerId customerId = CustomerId.newId();
    private final List<Seat> seats = List.of(new Seat(1, 1), new Seat(1, 2));
    private final LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

    @BeforeEach
    void setUp() {
        screeningId = ScreeningId.newId();
        var screening = new Screening(screeningId, "Oppenheimer", new Hall("B", 5, 10), LocalDateTime.now().plusDays(1));
        screeningRepository.save(screening);
    }

    @Test
    void shouldSaveAndReloadReservation() {
        var reservation = new Reservation(ReservationId.newId(), screeningId, customerId, seats, expiresAt);

        reservationRepository.save(reservation);
        var loaded = reservationRepository.findById(reservation.id()).orElseThrow();

        assertThat(loaded.id()).isEqualTo(reservation.id());
        assertThat(loaded.screeningId()).isEqualTo(screeningId);
        assertThat(loaded.customerId()).isEqualTo(customerId);
        assertThat(loaded.status()).isEqualTo(ReservationStatus.PENDING);
        assertThat(loaded.seats()).hasSize(2);
    }

    @Test
    void shouldPersistConfirmedStatus() {
        var reservation = new Reservation(ReservationId.newId(), screeningId, customerId, seats, expiresAt);
        reservation.confirm();

        reservationRepository.save(reservation);
        var loaded = reservationRepository.findById(reservation.id()).orElseThrow();

        assertThat(loaded.status()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void shouldPersistExpiredStatus() {
        var reservation = new Reservation(ReservationId.newId(), screeningId, customerId, seats, expiresAt);
        reservation.expire();

        reservationRepository.save(reservation);
        var loaded = reservationRepository.findById(reservation.id()).orElseThrow();

        assertThat(loaded.status()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void shouldFindReservationsByStatus() {
        var pending = new Reservation(ReservationId.newId(), screeningId, customerId, List.of(new Seat(2, 1)), expiresAt);
        var confirmed = new Reservation(ReservationId.newId(), screeningId, customerId, List.of(new Seat(2, 2)), expiresAt);
        confirmed.confirm();

        reservationRepository.save(pending);
        reservationRepository.save(confirmed);

        var pendingList = reservationRepository.findByStatus(ReservationStatus.PENDING);
        var confirmedList = reservationRepository.findByStatus(ReservationStatus.CONFIRMED);

        assertThat(pendingList).hasSize(1);
        assertThat(confirmedList).hasSize(1);
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        assertThat(reservationRepository.findById(ReservationId.newId())).isEmpty();
    }
}
