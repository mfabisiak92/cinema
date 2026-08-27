package com.cinema.reservation.infrastructure.scheduling;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ReservationExpirySchedulerIT extends PostgreSQLIntegrationTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ScreeningRepository screeningRepository;

    @Autowired
    ReservationExpiryScheduler scheduler;

    @Test
    void shouldExpireReservationAndReleaseSeat() {
        var screeningId = ScreeningId.newId();
        var screening = new Screening(screeningId, "Inception", new Hall("A", 3, 5), LocalDateTime.now().plusDays(1));
        screening.reserveSeat(1, 1);
        screeningRepository.save(screening);

        var expiresAt = LocalDateTime.now().minusMinutes(1);
        var reservation = Reservation.reconstitute(
                ReservationId.newId(), screeningId, CustomerId.newId(),
                List.of(new Seat(1, 1)), expiresAt, ReservationStatus.PENDING
        );
        reservationRepository.save(reservation);

        scheduler.expireReservations();

        var loadedReservation = reservationRepository.findById(reservation.id()).orElseThrow();
        assertThat(loadedReservation.status()).isEqualTo(ReservationStatus.EXPIRED);

        var loadedScreening = screeningRepository.findById(screeningId).orElseThrow();
        assertThat(loadedScreening.seats().stream()
                .filter(s -> s.row() == 1 && s.number() == 1)
                .findFirst()
                .map(Seat::isAvailable))
                .contains(true);
    }

    @Test
    void shouldNotExpireActiveReservations() {
        var screeningId = ScreeningId.newId();
        var screening = new Screening(screeningId, "Dune", new Hall("B", 3, 5), LocalDateTime.now().plusDays(1));
        screening.reserveSeat(1, 1);
        screeningRepository.save(screening);

        var expiresAt = LocalDateTime.now().plusMinutes(15);
        var reservation = new Reservation(
                ReservationId.newId(), screeningId, CustomerId.newId(),
                List.of(new Seat(1, 1)), expiresAt
        );
        reservationRepository.save(reservation);

        scheduler.expireReservations();

        var loaded = reservationRepository.findById(reservation.id()).orElseThrow();
        assertThat(loaded.status()).isEqualTo(ReservationStatus.PENDING);
    }
}
