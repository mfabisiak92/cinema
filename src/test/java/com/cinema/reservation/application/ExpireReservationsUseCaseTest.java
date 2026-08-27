package com.cinema.reservation.application;

import com.cinema.reservation.domain.CustomerId;
import com.cinema.reservation.domain.Reservation;
import com.cinema.reservation.domain.ReservationId;
import com.cinema.reservation.domain.ReservationStatus;
import com.cinema.screening.application.ScreeningRepository;
import com.cinema.screening.domain.Hall;
import com.cinema.screening.domain.Screening;
import com.cinema.screening.domain.ScreeningId;
import com.cinema.screening.domain.Seat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpireReservationsUseCaseTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ScreeningRepository screeningRepository;

    @InjectMocks
    ExpireReservationsUseCase useCase;

    private ScreeningId screeningId;
    private Screening screening;
    private Seat seat;

    @BeforeEach
    void setUp() {
        screeningId = ScreeningId.newId();
        screening = new Screening(screeningId, "Test Movie", new Hall("A", 3, 5), LocalDateTime.now().plusDays(1));
        seat = new Seat(1, 1);
        screening.reserveSeat(seat.row(), seat.number());
    }

    @Test
    void shouldExpireReservationPastExpiryTime() {
        var expiresAt = LocalDateTime.now().minusMinutes(1);
        var reservation = Reservation.reconstitute(
                ReservationId.newId(), screeningId, CustomerId.newId(),
                List.of(seat), expiresAt, ReservationStatus.PENDING
        );

        when(reservationRepository.findByStatus(ReservationStatus.PENDING)).thenReturn(List.of(reservation));
        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));

        useCase.execute();

        assertThat(reservation.status()).isEqualTo(ReservationStatus.EXPIRED);
        verify(reservationRepository).save(reservation);
        verify(screeningRepository).save(screening);
    }

    @Test
    void shouldReleaseSeatsOnScreeningWhenExpired() {
        var expiresAt = LocalDateTime.now().minusMinutes(1);
        var reservation = Reservation.reconstitute(
                ReservationId.newId(), screeningId, CustomerId.newId(),
                List.of(seat), expiresAt, ReservationStatus.PENDING
        );

        when(reservationRepository.findByStatus(ReservationStatus.PENDING)).thenReturn(List.of(reservation));
        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));

        useCase.execute();

        assertThat(screening.seats().stream().filter(s -> s.row() == 1 && s.number() == 1).findFirst())
                .map(Seat::isAvailable)
                .contains(true);
    }

    @Test
    void shouldSkipReservationsNotYetExpired() {
        var expiresAt = LocalDateTime.now().plusMinutes(10);
        var reservation = Reservation.reconstitute(
                ReservationId.newId(), screeningId, CustomerId.newId(),
                List.of(seat), expiresAt, ReservationStatus.PENDING
        );

        when(reservationRepository.findByStatus(ReservationStatus.PENDING)).thenReturn(List.of(reservation));

        useCase.execute();

        assertThat(reservation.status()).isEqualTo(ReservationStatus.PENDING);
        verify(reservationRepository, never()).save(any());
        verify(screeningRepository, never()).save(any());
    }

    @Test
    void shouldHandleMultiplePendingReservations() {
        var expiredAt = LocalDateTime.now().minusMinutes(1);
        var futureAt = LocalDateTime.now().plusMinutes(10);

        var expired = Reservation.reconstitute(
                ReservationId.newId(), screeningId, CustomerId.newId(),
                List.of(seat), expiredAt, ReservationStatus.PENDING
        );
        var active = Reservation.reconstitute(
                ReservationId.newId(), screeningId, CustomerId.newId(),
                List.of(new Seat(1, 2)), futureAt, ReservationStatus.PENDING
        );

        when(reservationRepository.findByStatus(ReservationStatus.PENDING)).thenReturn(List.of(expired, active));
        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));

        useCase.execute();

        assertThat(expired.status()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(active.status()).isEqualTo(ReservationStatus.PENDING);
        verify(reservationRepository, times(1)).save(expired);
        verify(reservationRepository, never()).save(active);
    }
}
