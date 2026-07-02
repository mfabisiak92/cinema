package com.cinema.reservation.application;

import com.cinema.reservation.domain.CustomerId;
import com.cinema.reservation.domain.Reservation;
import com.cinema.reservation.domain.ReservationId;
import com.cinema.screening.application.ScreeningRepository;
import com.cinema.screening.domain.Hall;
import com.cinema.screening.domain.Screening;
import com.cinema.screening.domain.ScreeningId;
import com.cinema.screening.domain.ScreeningNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

@ExtendWith(MockitoExtension.class)
class ReserveSeatUseCaseTest {

    @Mock
    ScreeningRepository screeningRepository;

    @Mock
    ReservationRepository reservationRepository;

    @InjectMocks
    ReserveSeatUseCase useCase;

    private final ScreeningId screeningId = new ScreeningId(UUID.randomUUID());
    private final CustomerId customerId = new CustomerId(UUID.randomUUID());
    private final Screening screening = new Screening(
            screeningId,
            "Dune",
            new Hall("A", 5, 10),
            LocalDateTime.now().plusDays(1)
    );

    private static ReserveSeatUseCase.SeatPosition pos(int row, int number) {
        return new ReserveSeatUseCase.SeatPosition(row, number);
    }

    @Test
    void shouldCreateReservationForAvailableSeats() {
        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        var command = new ReserveSeatUseCase.Command(screeningId, customerId, List.of(pos(1, 1), pos(1, 2)));

        ReservationId id = useCase.execute(command);

        var captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(id);
        assertThat(captor.getValue().screeningId()).isEqualTo(screeningId);
        assertThat(captor.getValue().seats()).hasSize(2);
    }

    @Test
    void shouldThrowWhenScreeningNotFound() {
        when(screeningRepository.findById(screeningId)).thenReturn(Optional.empty());
        var command = new ReserveSeatUseCase.Command(screeningId, customerId, List.of(pos(1, 1)));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ScreeningNotFoundException.class);
    }

    @Test
    void shouldThrowWhenSeatPositionDoesNotExist() {
        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        var command = new ReserveSeatUseCase.Command(screeningId, customerId, List.of(pos(99, 99)));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found or unavailable");
    }

    @Test
    void shouldThrowWhenSeatIsAlreadyReserved() {
        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        useCase.execute(new ReserveSeatUseCase.Command(screeningId, customerId, List.of(pos(1, 1))));

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        assertThatThrownBy(() -> useCase.execute(new ReserveSeatUseCase.Command(screeningId, customerId, List.of(pos(1, 1)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found or unavailable");
    }
}
