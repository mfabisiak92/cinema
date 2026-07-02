package com.cinema.reservation.application;

import com.cinema.reservation.domain.CustomerId;
import com.cinema.reservation.domain.Reservation;
import com.cinema.reservation.domain.ReservationId;
import com.cinema.reservation.domain.ReservationNotFoundException;
import com.cinema.reservation.domain.ReservationStatus;
import com.cinema.screening.domain.ScreeningId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cinema.screening.domain.Seat;

@ExtendWith(MockitoExtension.class)
class ConfirmReservationUseCaseTest {

    @Mock
    ReservationRepository reservationRepository;

    @InjectMocks
    ConfirmReservationUseCase useCase;

    private final ReservationId reservationId = new ReservationId(UUID.randomUUID());
    private final Reservation reservation = new Reservation(
            reservationId,
            new ScreeningId(UUID.randomUUID()),
            new CustomerId(UUID.randomUUID()),
            List.of(new Seat(1, 1)),
            LocalDateTime.now().plusMinutes(15)
    );

    @Test
    void shouldConfirmPendingReservation() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        useCase.execute(new ConfirmReservationUseCase.Command(reservationId));

        assertThat(reservation.status()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void shouldThrowWhenReservationNotFound() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ConfirmReservationUseCase.Command(reservationId)))
                .isInstanceOf(ReservationNotFoundException.class);
    }
}
